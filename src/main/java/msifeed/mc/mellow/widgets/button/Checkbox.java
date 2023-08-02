package msifeed.mc.mellow.widgets.button;

import msifeed.mc.mellow.Mellow;
import msifeed.mc.mellow.render.RenderParts;
import msifeed.mc.mellow.theme.Part;
import msifeed.mc.mellow.utils.SizePolicy;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class Checkbox extends Button {
    private final Part offPart = Mellow.getPart("checkbox_off");
    private final Part onPart = Mellow.getPart("checkbox_on");

    private boolean checked = false;
    private Checkbox.Group group = null;

    private Consumer<Boolean> onChange = b -> {};

    public Checkbox() {
        Part offPart = getOffPart();
        setSizeHint(offPart.size.x, offPart.size.y);
        setSizePolicy(SizePolicy.Policy.FIXED, SizePolicy.Policy.FIXED);
        setZLevel(1);
    }

    public Checkbox(boolean checked) {
        this();
        setChecked(checked);
    }

    public Checkbox(Checkbox.Group group) {
        this();
        setGroup(group);
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
            if (group != null)
                group.setChecked(this, checked);
            this.checked = checked;
            this.onChange.accept(checked);
        }
    }

    public void setGroup(Checkbox.Group group) {
        this.group = group;
        group.add(this);
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

    public static class Group {
//        private final Set<Checkbox> checkboxes = new HashSet<>();
        private final Set<Checkbox> selected = new HashSet<>();

        public boolean multiCheck = false;

        public void setChecked(Checkbox checkbox, boolean checked) {
            if (!multiCheck) {
                for (Checkbox c : selected)
                    c.checked = false;
                selected.clear();
            }

            if (checked)
                selected.add(checkbox);
            else
                selected.remove(checkbox);
        }

        void add(Checkbox checkbox) {
//            checkboxes.add(checkbox);
        }
    }
}
