package msifeed.mc.mellow.widgets.button;

import msifeed.mc.mellow.Mellow;
import msifeed.mc.mellow.layout.AnchorLayout;
import msifeed.mc.mellow.render.RenderParts;
import msifeed.mc.mellow.theme.Part;
import msifeed.mc.mellow.widgets.text.Label;

public class ButtonLabel extends Button {
    private Part normalPart = Mellow.getPart("button_normal");
    private Part hoverPart = Mellow.getPart("button_hover");
    private Part pressPart = Mellow.getPart("button_press");
    private Part disabledPart = Mellow.getPart("button_disabled");

    protected Label label = new Label();

    public ButtonLabel() {
        getMargin().set(3, 4, 6, 4);
        setLayout(new AnchorLayout());

        label.setZLevel(1);
        addChild(label);
    }

    public ButtonLabel(String text) {
        this();
        setLabel(text);
    }

    public String getLabel() {
        return label.getText();
    }

    public void setLabel(String text) {
        this.label.setText(text);
    }

    public void setColor(int color) {
        this.label.setColor(color);
    }

    @Override
    protected void renderSelf() {
        if (isDisabled())
            RenderParts.nineSlice(disabledPart, getGeometry());
        else if (isPressed())
            RenderParts.nineSlice(pressPart, getGeometry());
        else if (isHovered())
            RenderParts.nineSlice(hoverPart, getGeometry());
        else
            RenderParts.nineSlice(normalPart, getGeometry());
    }
}
