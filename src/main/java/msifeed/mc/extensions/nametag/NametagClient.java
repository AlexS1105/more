package msifeed.mc.extensions.nametag;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.ReflectionHelper;
import msifeed.mc.sys.rpc.RpcMethodHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.HashMap;

public class NametagClient extends Nametag {
    private static final int MAX_NAMETAG_DISTANCE = 4;
    private static final int TYPING_PING_MS = 3000;
    private static final int TYPING_TAG_INTERVAL_MS = 400;
    private static final String[] TYPING_REPLACER = {".", "..", "..."};

    private final HashMap<Integer, Long> typingPlayers = new HashMap<>();
    private long lastNotify = 0;
    private char lastCharPressed = 0;
    private boolean chatIsOpened = false;

    @RpcMethodHandler(broadcastTyping)
    public void onBroadcastTyping(int id) {
        typingPlayers.put(id, System.currentTimeMillis());
    }

    @SubscribeEvent
    public void onRenderLivingSpecialPre(RenderLivingEvent.Specials.Pre event) {
        if (!(event.entity instanceof EntityPlayer)) return;

        final EntityPlayer self = Minecraft.getMinecraft().thePlayer;
        final EntityPlayer player = (EntityPlayer) event.entity;

        final Long typingStarted = typingPlayers.get(player.getEntityId());
        boolean isTyping = false;
        if (typingStarted != null && !displayOriginalUsername()) {
            final long now = System.currentTimeMillis();
            if (now - typingStarted > TYPING_PING_MS) {
                // End typing
                typingPlayers.remove(player.getEntityId());
                player.refreshDisplayName();
            } else {
                // Refresh typing
                final String dots = TYPING_REPLACER[(int) (now / TYPING_TAG_INTERVAL_MS % TYPING_REPLACER.length)];
                ReflectionHelper.setPrivateValue(EntityPlayer.class, player, dots, "displayname");
                isTyping = true;
            }
        } else {
            // Show nicknames
            final String name = displayOriginalUsername() ? player.getCommandSenderName() : getPreferredName(player);
            if (!name.equals(player.getDisplayName()))
                ReflectionHelper.setPrivateValue(EntityPlayer.class, player, name, "displayname");
        }

        final float distance = self.getDistanceToEntity(player);
        final int visibleRange = isTyping ? getSpeechRadius() : MAX_NAMETAG_DISTANCE;
        if (distance > visibleRange)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void onRenderLivingSpecialPost(RenderLivingEvent.Specials.Post event) {
        if (!(event.entity instanceof EntityPlayer)) return;
        final EntityPlayer player = (EntityPlayer) event.entity;

        String status = getStatus(player);

        if (status == null) return;

        // Get the render manager and font renderer
        RenderManager renderManager = RenderManager.instance;
        FontRenderer fontRenderer = renderManager.getFontRenderer();

        // Get the player's position and distance from the camera
        double x = player.lastTickPosX + (player.posX - player.lastTickPosX);
        double y = player.lastTickPosY + (player.posY - player.lastTickPosY) + 3f;
        double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ);
        double distance = player.getDistanceSqToEntity(renderManager.livingPlayer);

        // Check if the player is in range and visible
        if (distance < 100 && player.isEntityAlive() && !player.isInvisibleToPlayer((EntityPlayer) renderManager.livingPlayer)) {
            // Push a new matrix to the stack
            GL11.glPushMatrix();
            // Translate to the player's position
            GL11.glTranslated(x - RenderManager.renderPosX, y - RenderManager.renderPosY, z - RenderManager.renderPosZ);
            // Rotate the matrix to face the camera
            GL11.glNormal3f(0.0F, 1.0F, 0.0F);
            GL11.glRotatef(-renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
            // Scale the matrix according to the distance
            GL11.glScalef(-0.025F, -0.025F, 0.025F);
            // Disable lighting and depth testing
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glDepthMask(false);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            // Enable blending and alpha testing
            GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            // Draw a dark background for the text
            Tessellator tessellator = Tessellator.instance;
            int width = fontRenderer.getStringWidth(status) / 2;
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            tessellator.startDrawingQuads();
            tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, 0.25F);
            tessellator.addVertex(-width - 1, -1 + 10.5f, 0.0D);
            tessellator.addVertex(-width - 1, 8 + 10.5f, 0.0D);
            tessellator.addVertex(width + 1, 8 + 10.5f, 0.0D);
            tessellator.addVertex(width + 1, -1 + 10.5f, 0.0D);
            tessellator.draw();
            GL11.glEnable(GL11.GL_TEXTURE_2D);

            // Draw the status text in white
            fontRenderer.drawString(status, -fontRenderer.getStringWidth(status) / 2, (int)10.5f, 553648127);

            // Restore the previous settings
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
            fontRenderer.drawString(status, -fontRenderer.getStringWidth(status) / 2, (int)10.5f, -1);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glDisable(GL11.GL_BLEND);

            // Pop the matrix from the stack
            GL11.glPopMatrix();
        }
    }


    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && chatIsOpened)
            chatIsOpened = Minecraft.getMinecraft().currentScreen instanceof GuiChat;
    }

    @SubscribeEvent
    public void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!(Minecraft.getMinecraft().currentScreen instanceof GuiChat))
            return;

        final char c = Keyboard.getEventCharacter();
        if (lastCharPressed != c) {
            if (chatIsOpened && c != 0)
                sendNotifyTyping();
            chatIsOpened = true;
            lastCharPressed = c;
        }
    }

    @SubscribeEvent
    public void onNameFormat(PlayerEvent.NameFormat event) {
        super.onNameFormat(event);
    }

    private void sendNotifyTyping() {
        if (System.currentTimeMillis() - lastNotify < TYPING_PING_MS / 2)
            return;
        lastNotify = System.currentTimeMillis();
        Nametag.notifyTyping();
    }

    private static boolean displayOriginalUsername() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU);
    }
}
