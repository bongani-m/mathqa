package dekauliya.fyp.mathqa.Utils;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.orhanobut.logger.Logger;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.List;

import dekauliya.fyp.mathqa.R;
import dekauliya.fyp.mathqa.Views.SearchViews.FormulaHelper;
import dekauliya.fyp.mathqa.Views.SearchViews.FormulaType;
import dekauliya.fyp.mathqa.Views.SearchViews.SearchActivity_;
import dekauliya.fyp.mathqa.Views.SearchViews.SearchType;
import io.github.kexanie.library.MathView;

/**
 * Created by dekauliya on 11/2/17.
 *
 * Utilities for inputting search queries which involves text queries, and formula search queries.
 */
@EBean
public class SearchDialogUtils {
    @RootContext
    Activity activity;

    // Formula Related
    private List<String> formulaStrs;
    private List<String> formulas;
    private FormulaType formulaType;
    private static FormulaType[] formulaTypes = FormulaType.values();

    LinearLayout fsLLDialogContainer;
    RelativeLayout fsLLBtnContainer;
    MathView fsMathPreview;
    MaterialEditText fsEditText;
    TextView fsPreviewPlaceholder;
    MDButton fsResetBtn;
    MDButton fsInsertBtn;

    MaterialDialog formulaPreviewDialog;

    boolean databaseSearch = false;

    String textQuery;
    String formulaQuery;

    public void displaySearchDialog(String query){
        new MaterialDialog.Builder(activity)
                .title(R.string.sd_search_text_title)
                .titleColorRes(R.color.colorAccent)
                .positiveText("Text Search")
                .positiveColorRes(R.color.colorAccent)
                .negativeText("Cancel")
                .negativeColorRes(R.color.colorAccent)
                .checkBoxPrompt("Exact database search", false, null)
                .input(activity.getString(R.string.sd_text_hint),
                        query, false,
                        new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                textQuery = input.toString();
                                if (dialog.isPromptCheckBoxChecked()) {
                                    SearchActivity_.intent(activity)
                                            .searchTypeExtra(SearchType.TEXT_DB)
                                            .searchQuery(textQuery)
                                            .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            .start();
                                }else{
                                    SearchActivity_.intent(activity)
                                            .searchTypeExtra(SearchType.FULL_TEXT)
                                            .searchQuery(textQuery)
                                            .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                            .start();
                                }
                            }
                        }).show();
    }

    public void displayFormulaInputPreview(String query) {
        formulaPreviewDialog = new MaterialDialog.Builder(activity)
                .title(activity.getString(R.string.sd_formula_search_title))
                .titleColorRes(R.color.colorAccent)
                .customView(R.layout.dialog_formula_input_preview, true)
                .positiveText(R.string.search_action)
                .negativeText(R.string.cancel_action)
                .autoDismiss(false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                        Logger.d("Dialog, textQuery: " + formulaQuery);
                        SearchActivity_.intent(activity)
                                .searchTypeExtra(SearchType.FORMULA)
                                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                                .searchQuery(formulaQuery)
                                .start();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).build();

        View formulaView = formulaPreviewDialog.getCustomView();
        fsLLDialogContainer = (LinearLayout) formulaView;
        View childView;

        for(int i = 0; i< fsLLDialogContainer.getChildCount(); i++){
            childView = fsLLDialogContainer.getChildAt(i);
            if ( childView.getId() == R.id.fs_latex_preview ){
                fsMathPreview = (MathView) childView;
            }else if ( childView.getId() == R.id.fs_latex_input){
                fsEditText = (MaterialEditText) childView;
                fsEditText.setText(query);
            }else if (childView.getId() == R.id.fs_preview_placeholder){
                fsPreviewPlaceholder = (TextView) childView;
            }else if (childView.getId() == R.id.fs_btn_container){
                fsLLBtnContainer = (RelativeLayout) childView;
            }
        }

        toggleActionButton(formulaPreviewDialog, fsEditText.getText().toString());

        fsEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                formulaQuery = editable.toString();
                toggleActionButton(formulaPreviewDialog, formulaQuery);
                ViewUtils.displayLatex(fsMathPreview, fsPreviewPlaceholder, formulaQuery, true);
            }
        });

        MDButton mdButton;
        for(int i = 0; i < fsLLBtnContainer.getChildCount(); i++){
            childView = fsLLBtnContainer.getChildAt(i);
            if (childView instanceof MDButton){
                mdButton = (MDButton) childView;
                mdButton.setAllCapsCompat(true);
                mdButton.setTypeface(ViewUtils.getTypeface(TypefaceStyle.MEDIUM));
                mdButton.setTextColor(ContextCompat.getColor(activity, R.color.colorAccent));

                if (childView.getId() == R.id.fs_reset_btn){
                    fsResetBtn = mdButton;
                    fsResetBtn.setText("Reset");
                    fsResetBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            fsEditText.setText("");
                        }
                    });
                }else if (childView.getId() == R.id.fs_insert_formula_btn){
                    fsInsertBtn = mdButton;
                    fsInsertBtn.setText("Insert Formula");
                    fsInsertBtn.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View view) {
                            displayFormulaOptions();
                        }
                    });
                }
            }
        }

        formulaStrs = FormulaHelper.getFormulaStrArray(FormulaType.ALL, activity);
        formulas = FormulaHelper.getFormulaArray(FormulaType.ALL, activity);
        formulaPreviewDialog.show();
    }

    private void toggleActionButton(MaterialDialog dialog, String input) {
        if (input.length() > 0){
            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(true);
        } else{
            dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
        }
    }

    private void displayFormulaOptions() {
        new MaterialDialog.Builder(activity)
                .title(activity.getString(R.string.sd_select_formula_category))
                .titleColorRes(R.color.colorAccent)
                .items(R.array.formula_title)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        formulaType = formulaTypes[which];
                        formulaStrs = FormulaHelper.getFormulaStrArray(formulaType, activity);
                        formulas = FormulaHelper.getFormulaArray(formulaType, activity);
                        displayFormulaList();
                    }
                }).show();
    }

    private void displayFormulaList() {
        new MaterialDialog.Builder(activity)
                .title(activity.getString(R.string.sd_select_formula))
                .titleColorRes(R.color.colorAccent)
                .items(formulaStrs)
                .itemsCallback(new MaterialDialog.ListCallback() {
                    @Override
                    public void onSelection(MaterialDialog dialog, View view, int which,
                                            CharSequence text) {
                        if (formulas != null && which >= 0 && which < formulas.size()) {
                            int cursorPos = fsEditText.getSelectionEnd();
                            StringBuilder editTextStr = new StringBuilder(fsEditText.getText());
                            editTextStr.insert(cursorPos, formulas.get(which));
                            fsEditText.setText(editTextStr.toString());
                            fsEditText.setSelection(editTextStr.length());
                        }
                    }
                }).show();
    }
}
