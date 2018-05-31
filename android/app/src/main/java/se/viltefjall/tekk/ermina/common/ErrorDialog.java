package se.viltefjall.tekk.ermina.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;

import se.viltefjall.tekk.ermina.SelectDevice.SelectDeviceActivity;

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
                    Intent intent = new Intent(mActivity, SelectDeviceActivity.class);
                    mActivity.startActivity(intent);
                }
            });
        }
        dialog.show();
    }
}
