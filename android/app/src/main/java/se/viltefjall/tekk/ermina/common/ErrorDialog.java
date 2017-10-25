package se.viltefjall.tekk.ermina.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class ErrorDialog {
    @SuppressWarnings("unused")
    public static final String ID = "ErrorDialog";

    private AlertDialog.Builder mBuilder;
    private Activity            mActivity;

    public ErrorDialog(Activity activity) {
        mBuilder  = new AlertDialog.Builder(activity);
        mActivity = activity;
    }

    public void displayError(String message, boolean finishOnDismiss) {
        AlertDialog dialog;
        mBuilder.setMessage(message);
        dialog = mBuilder.create();

        if(finishOnDismiss) {
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mActivity.finish();
                }
            });
        }
        dialog.show();
    }
}
