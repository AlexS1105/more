package msifeed.mc.mellow.widgets.button;

import msifeed.mc.mellow.Mellow;
import msifeed.mc.mellow.render.RenderParts;
import msifeed.mc.mellow.theme.Part;
import msifeed.mc.mellow.utils.SizePolicy;

import java.util.function.Consumer;

public class Crossbox extends Button {
    private final Part offPart = Mellow.getPart("crossbox_off");
    private final Part onPart = Mellow.getPart("crossbox_on");

    private boolean checked = false;
    private Consumer<Boolean> onChange = b -> {};

    public Crossbox() {
        Part offPart = getOffPart();
        setSizeHint(offPart.size.x, offPart.size.y);
        setSizePolicy(SizePolicy.Policy.FIXED, SizePolicy.Policy.FIXED);
        setZLevel(1);
    }

    public Crossbox(boolean isActive) {
        this();
        setChecked(isActive);
    }

    public Part getOffPart() {
        return offPart;
    }

    public Part getOnPart() {
        return onPart;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        if (this.checked != checked) {
            this.checked = checked;
            this.onChange.accept(checked);
        }
    }

    public void setCallback(Consumer<Boolean> onChange) {
        this.onChange = onChange;
    }

    @Override
    protected void renderSelf() {
        RenderParts.slice(isChecked() ? getOnPart() : getOffPart(), getGeometry());
    }

    @Override
    public void onClick(int xMouse, int yMouse, int button) {
        if (!isDisabled())
            setChecked(!isChecked());
    }
}
