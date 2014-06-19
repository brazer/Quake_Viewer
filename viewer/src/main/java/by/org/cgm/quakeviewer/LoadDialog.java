package by.org.cgm.quakeviewer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import by.org.cgm.quakeviewer.quake.QuakeContent;

public class LoadDialog extends DialogFragment {

    private Context context;

    public void setContext(Context c) {
        context = c;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_start, null);
        builder.setView(v)
                .setTitle("Загрузка данных")
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        QuakeListActivity.isLoaded = true;
                        if (QuakeContent.QUAKES.size()==0) System.exit(0);
                        else dismiss();
                    }
                });
        Button btnLocal = (Button) v.findViewById(R.id.btn_local);
        btnLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuakeListActivity.isLoaded = true;
                showOpenFileDialog();
                dismiss();
            }
        });
        Button btnInternet = (Button) v.findViewById(R.id.btn_internet);
        btnInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QuakeListActivity.isLoaded = true;
                showInternetDialog();
                dismiss();
            }
        });
        return builder.create();
    }

    private void showOpenFileDialog() {
        try {
            QuakeListActivity.fileDialog = new OpenFileDialog(context);
            QuakeListActivity.fileDialog.setFolderIcon(getResources().getDrawable(R.drawable.abc_ic_go));
            QuakeListActivity.fileDialog.setOpenDialogListener(QuakeListActivity.listener);
            QuakeListActivity.isLoadedFileDialog = false;
            QuakeListActivity.fileDialog.show();
        } catch(IllegalStateException e) {
            e.printStackTrace();

        }
    }

    private void showInternetDialog() {
        QuakeListActivity.isLoadedInternetDialog = false;
        QuakeListActivity.internetDialog.show(getFragmentManager(), null);
    }

}
