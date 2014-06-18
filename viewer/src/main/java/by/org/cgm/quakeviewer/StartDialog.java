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
import android.widget.TextView;
import android.widget.Toast;

import by.org.cgm.jdbf.JdbfTask;
import by.org.cgm.quakeviewer.quake.QuakeContent;

public class StartDialog extends DialogFragment implements OpenFileDialog.OpenDialogListener {

    private Context context;
    private OnTaskCompleteListener listener;

    public void setContext(Context c) {
        context = c;
    }

    public void setListener(OnTaskCompleteListener l) {
        listener = l;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View v = inflater.inflate(R.layout.dialog_start, null);
        builder.setView(v)
                .setTitle("Загрузка данных")
                .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (QuakeContent.ITEMS.size()==0) System.exit(0);
                        else dismiss();
                    }
                });
        Button btnLocal = (Button) v.findViewById(R.id.btn_local);
        btnLocal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createOpenFileDialog();
                dismiss();
            }
        });
        Button btnInternet = (Button) v.findViewById(R.id.btn_internet);
        btnInternet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createInternetDialog();
                dismiss();
            }
        });
        return builder.create();
    }

    private void createOpenFileDialog() {
        OpenFileDialog dialog = new OpenFileDialog(context);
        dialog.setOpenDialogListener(this);
        dialog.setFolderIcon(context.getResources().getDrawable(R.drawable.abc_ic_go));
        dialog.show();
    }

    @Override
    public void OnSelectedFile(String fileName) {
        if (!fileName.contains("dbf")) {
            Toast.makeText(context, "Выберите dbf-файл", Toast.LENGTH_SHORT).show();
            createOpenFileDialog();
        }
        else {
            AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager(context, listener);
            mAsyncTaskManager.setupTask(new JdbfTask(context.getResources()), fileName);
        }
    }

    private void createInternetDialog() {
        InternetDialog dialog = new InternetDialog();
        dialog.show(getFragmentManager(), null);
    }

    private class InternetDialog extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            LayoutInflater inflater = getActivity().getLayoutInflater();
            final View v = inflater.inflate(R.layout.dialog_internet, null);
            final TextView textView = (TextView) v.findViewById(R.id.txtHttp);
            builder.setView(v)
                    .setTitle("Загрузка данных из интернета")
                    .setPositiveButton("ОК", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String http = textView.getText().toString();
                            loadFromHttp(http);
                            dismiss();
                        }
                    })
                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismiss();
                        }
                    });
            return builder.create();
        }

        private void loadFromHttp(String url) {
            AsyncTaskManager mAsyncTaskManager = new AsyncTaskManager(context, listener);
            mAsyncTaskManager.setupTask(new JdbfTask(context.getResources()), url);
        }

    }

}
