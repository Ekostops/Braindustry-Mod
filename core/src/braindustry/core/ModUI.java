package braindustry.core;

import braindustry.gen.BDTex;
import braindustry.ui.dialogs.cheat.ModCheatMenu;
import braindustry.ui.dialogs.ModOtherSettingsDialog;
import braindustry.ui.dialogs.ModSettingsDialog;
import arc.ApplicationListener;
import arc.Core;
import arc.Input;
import arc.KeyBinds;
import arc.func.Cons;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.ui.Dialog;
import arc.scene.ui.TextField;
import arc.scene.ui.layout.Collapser;
import arc.util.Disposable;
import arc.util.Log;
import arc.util.Strings;
import arc.util.Time;
import braindustry.gen.BDTex;
import braindustry.gen.Stealthc;
import braindustry.input.ModBinding;
import braindustry.ui.ModStyles;
import braindustry.ui.dialogs.BackgroundStyleDialog;
import braindustry.ui.fragments.ModHudFragment;
import braindustry.ui.fragments.ModMenuFragment;
import mindustry.Vars;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mma.ui.dialogs.ModColorPicker;

import static braindustry.core.CheatUI.*;
import static braindustry.BDVars.*;
import static arc.Core.settings;
import static braindustry.input.ModBinding.*;
import static mindustry.Vars.headless;
import static mindustry.Vars.ui;

public class ModUI extends mma.core.ModUI implements Disposable, ApplicationListener {
    static {
        //x axis or not
        ModMenuFragment.xAxis(true);
        //wave size in pixels
        ModMenuFragment.pixels(3f);
        //wave force
        ModMenuFragment.otherAxisMul(50);
        //waves speed
        ModMenuFragment.timeScl(0.1f);

    }

    public ModColorPicker colorPicker;
    public BackgroundStyleDialog backgroundStyleDialog;
    public ModOtherSettingsDialog otherSettingsDialog;
    public ModSettingsDialog settingsDialog;
    private boolean inited=false;

    public ModUI() {
        super(ModBinding.values());
    }

    @Override
    public void init() {
        if (headless) return;
        inited=true;
        inTry(BDTex::load);
        inTry(ModStyles::load);
        inTry(ModMenuFragment::init);
        inTry(ModHudFragment::init);
        new ModCheatMenu((table) -> {
            table.button("@cheat-menu.title", () -> {
                BaseDialog dialog = new BaseDialog("@cheat-menu.title");
                dialog.cont.table((t) -> {
                    t.background(Styles.none);
                    t.defaults().size(280.0F, 60.0F);
                    t.button("@cheat-menu.change-team", CheatUI::openTeamChooseDialog).growX().get().setStyle(ModStyles.buttonPaneTopt);
                    t.row();
                    t.button("@cheat-menu.change-unit", CheatUI::openUnitChooseDialog).growX().get().setStyle(ModStyles.buttonPanet);
                    t.row();
                    if (!Vars.net.client()) {
                        t.button("@cheat-menu.edit-rules", CheatUI::openRulesEditDialog).growX().get().setStyle(ModStyles.buttonPanet);
                        t.row();
                    }
                    t.button("@cheat-menu.items-manager", CheatUI::openModCheatItemsMenu).growX().get().setStyle(ModStyles.buttonPanet);
                    t.row();
                    t.button("@cheat-menu.unlock-content", CheatUI::openUnlockContentDialog).growX().get().setStyle(ModStyles.buttonPaneBottomt);
                    t.row();
                });
//                dialog.cont
//                t.background(Styles.none);
                dialog.addCloseListener();
                dialog.addCloseButton();
                dialog.show();

            }).size(280.0f / 2f, 60.0F).get().setStyle(ModStyles.buttonEdge3t);
//            table.visibility = () -> CheatUI.visibility.get();
        });

        colorPicker = new ModColorPicker();
        backgroundStyleDialog = new BackgroundStyleDialog();
        otherSettingsDialog = new ModOtherSettingsDialog();
        settingsDialog = new ModSettingsDialog();
    }

    @Override
    public void dispose() {
    }
    @Override
    public void update() {
        if (!inited)return;
        boolean noDialog = !Core.scene.hasDialog();
        boolean inGame = Vars.state.isGame();

        boolean inMenu = Vars.state.isMenu() || !ui.planet.isShown();
        if (!ui.controls.isShown()) {
            if (Core.input.keyTap(show_unit_dialog) && noDialog && inGame) {
                openUnitChooseDialog();
            } else if (Core.input.keyTap(show_team_dialog) && noDialog && inGame) {
                openTeamChooseDialog();
            } else if (Core.input.keyTap(show_unlock_dialog) && !inMenu) {
                openUnlockContentDialog();
            } else if (Core.input.keyTap(show_item_manager_dialog) && noDialog) {
                openModCheatItemsMenu();
            } else if (Core.input.keyTap(show_rules_edit_dialog) && inGame && noDialog) {
                openRulesEditDialog();
            }
        }
        if (inGame && Vars.state.isPaused() && Vars.player.unit() instanceof Stealthc) {
            Stealthc unit = (Stealthc) Vars.player.unit();
            unit.updateStealthStatus();
        }
    }
    public static void showExceptionDialog(Throwable t) {
        showExceptionDialog("", t);
    }
    public static Dialog getInfoDialog(String title, String subTitle, String message, Color lineColor) {
        return new Dialog(title) {{
                setFillParent(true);
                cont.margin(15.0F);
                cont.add(subTitle);
                cont.row();
                cont.image().width(300.0F).pad(2.0F).height(4.0F).color(lineColor);
                cont.row();
                cont.add(message).pad(2.0F).growX().wrap().get().setAlignment(1);
                cont.row();
                cont.button("@ok", this::hide).size(120.0F, 50.0F).pad(4.0F);
                closeOnBack();
            }};
    }
    public static void showExceptionDialog(final String text, final Throwable exc) {
        new Dialog("") {{
                String message = Strings.getFinalMessage(exc);
                setFillParent(true);
                cont.margin(15.0F);
                cont.add("@error.title").colspan(2);
                cont.row();
                cont.image().width(300.0F).pad(2.0F).colspan(2).height(4.0F).color(Color.scarlet);
                cont.row();
                cont.add((text.startsWith("@") ? Core.bundle.get(text.substring(1)) : text) + (message == null ? "" : "\n[lightgray](" + message + ")")).colspan(2).wrap().growX().center().get().setAlignment(1);
                cont.row();
                Collapser col = new Collapser((base) -> {
                    base.pane((t) -> {
                        t.margin(14.0F).add(Strings.neatError(exc)).color(Color.lightGray).left();
                    });
                }, true);
                cont.button("@details", Styles.togglet, col::toggle).size(180.0F, 50.0F).checked((b) -> {
                    return !col.isCollapsed();
                }).fillX().right();
                cont.button("@ok", this::hide).size(110.0F, 50.0F).fillX().left();
                cont.row();
                cont.add(col).colspan(2).pad(2.0F);
                closeOnBack();
            }}.show();
    }
    public static void showTextInput(String title, String text, String def, Cons<String> confirmed) {
        showTextInput(title, text, 32, def, confirmed);
    }

    public static void showTextInput(String titleText, String text, int textLength, String def, Cons<String> confirmed) {
        showTextInput(titleText, text, textLength, def, (t,c)->{return true;}, confirmed);
    }
    public static void showTextInput(final String titleText, final String dtext, final int textLength, final String def, final TextField.TextFieldFilter filter, final Cons<String> confirmed) {
        if (Vars.mobile) {
            Core.input.getTextInput(new Input.TextInput() {
                {
                    title = titleText.startsWith("@") ? Core.bundle.get(titleText.substring(1)) : titleText;
                    text = def;
                    numeric=filter== TextField.TextFieldFilter.digitsOnly;
//                    numeric = inumeric;
                    maxLength = textLength;
                    accepted = confirmed;
                }
            });
        } else {
            new Dialog(titleText) {{
                cont.margin(30.0F).add(dtext).padRight(6.0F);
                TextField field = cont.field(def, (t) -> {
                }).size(330.0F, 50.0F).get();
                field.setFilter((f, c) -> {
                    return field.getText().length() < textLength && filter.acceptChar(f, c);
                });
                buttons.defaults().size(120.0F, 54.0F).pad(4.0F);
                buttons.button("@cancel", this::hide);
                buttons.button("@ok", () -> {
                    confirmed.get(field.getText());
                    hide();
                }).disabled((b) -> {
                    return field.getText().isEmpty();
                });
                keyDown(KeyCode.enter, () -> {
                    String text = field.getText();
                    if (!text.isEmpty()) {
                        confirmed.get(text);
                        hide();
                    }

                });
                keyDown(KeyCode.escape, this::hide);
                keyDown(KeyCode.back, this::hide);
                show();
                Core.scene.setKeyboardFocus(field);
                field.setCursorPosition(def.length());
            }};
        }

    }

}
