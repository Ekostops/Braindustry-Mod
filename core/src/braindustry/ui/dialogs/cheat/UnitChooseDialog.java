package braindustry.ui.dialogs.cheat;

import arc.Core;
import arc.func.Boolf;
import arc.graphics.Color;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Scl;
import arc.scene.ui.layout.Table;
import braindustry.core.ModUI;
import mindustry.Vars;
import mindustry.content.UnitTypes;
import mindustry.type.UnitType;
import mindustry.ui.dialogs.BaseDialog;

import static mindustry.Vars.mobile;

public class UnitChooseDialog extends BaseDialog {
    private Boolf<UnitType> check;

    public UnitChooseDialog(Boolf<UnitType> check) {
        super("Choose unit:");
        this.check = check;
        setup();
        if (mobile) onResize(this::setup);
        this.addCloseButton();
    }

    private void setup() {
        cont.clear();
        Table table = new Table();
        float pad = 6f;
        float coln = 5;
        final float buttonSize = !mobile ? 100f : (Core.graphics.getWidth()-Scl.scl((coln+1)*pad)-20) / Scl.scl(coln);
        ScrollPane pane = new ScrollPane(table);
        pane.setScrollingDisabled(true, false);
        int[] index = {0};
        for (UnitType unitType : Vars.content.units().select(u -> !u.isHidden())) {
            if (unitType == UnitTypes.block) continue;
            if (index[0] % coln == 0) table.row();
            index[0]++;
            Button button = new Button();
            button.clearChildren();
            Image image = new Image(unitType.region);
            Cell<Image> imageCell = button.add(image);
            float imageSize = buttonSize * 0.7f;
            if (image.getWidth() == image.getHeight()) {
                imageCell.size(imageSize);
            } else {
                float width = image.getWidth(), height = image.getHeight();

                imageCell.size(imageSize * (width / height), imageSize);
            }

            if (unitType != UnitTypes.block) {
                button.clicked(() -> {
                    if (check.get(unitType)) this.hide();

                });
            } else {
                button.clicked(() -> {
                    ModUI.getInfoDialog("", "Don't use Block unit", "", Color.scarlet).show();
                });
            }
            table.add(button).size(buttonSize).pad(pad);
        }
        this.cont.add(pane).growY().growX().bottom().center();

    }


}