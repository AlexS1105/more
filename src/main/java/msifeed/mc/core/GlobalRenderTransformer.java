package msifeed.mc.core;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.CheckClassAdapter;

public class GlobalRenderTransformer implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (name.equals("net.minecraft.client.renderer.RenderGlobal")) {
            ClassReader cr = new ClassReader(basicClass);
            ClassWriter cw = new ClassWriter(0);
            CheckClassAdapter cca = new CheckClassAdapter(cw, true);
            GlobalRenderPatcher grp = new GlobalRenderPatcher(cca);
            cr.accept(grp, 0);
            return cw.toByteArray();
        }
        return basicClass;
    }
}


