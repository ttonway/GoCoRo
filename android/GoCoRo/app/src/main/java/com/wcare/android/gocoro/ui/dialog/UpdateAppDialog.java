package com.wcare.android.gocoro.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.Toast;

import com.wcare.android.gocoro.R;
import com.wcare.android.gocoro.utils.SPManager;


public class UpdateAppDialog extends DialogFragment {
    private static final String TAG = UpdateAppDialog.class.getSimpleName();

    private static final String KEY_URL = "UpdateAppDialogFrag:url";
    private static final String KEY_UPDATE_INFO = "UpdateAppDialogFrag:update-info";

    public static UpdateAppDialog newInstance(String url, String msg) {
        UpdateAppDialog frag = new UpdateAppDialog();
        Bundle b = new Bundle();
        b.putString(KEY_URL, url);
        b.putString(KEY_UPDATE_INFO, msg);
        frag.setArguments(b);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String message = getArguments().getString(KEY_UPDATE_INFO);
        if (message == null) {
            message = getString(R.string.have_new_app_version);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(R.string.btn_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                downloadApp(getActivity(), getArguments().getString(KEY_URL));
                            }
                        }
                )
                .setNegativeButton(R.string.btn_cancel, null);
        return builder.create();
    }

    public static void downloadApp(Context context, String url) {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Toast.makeText(context, R.string.sdcard_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(url);

            long downloadId = SPManager.getLong(context, SPManager.KEY_USER_APK_DOWNLOAD_ID, -1);
            if (downloadId != -1) {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                // query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
                Cursor c = downloadManager.query(query);
                try {
                    if (c != null && c.moveToFirst()) {
                        String uriStr = c.getString(c
                                .getColumnIndex(DownloadManager.COLUMN_URI));
                        if (uri.compareTo(Uri.parse(uriStr)) == 0) {
                            String localUri2 = c
                                    .getString(c
                                            .getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
                            int status = c
                                    .getInt(c
                                            .getColumnIndex(DownloadManager.COLUMN_STATUS));
                            if (status == DownloadManager.STATUS_PAUSED
                                    || status == DownloadManager.STATUS_PENDING
                                    || status == DownloadManager.STATUS_RUNNING) {
                                // downloading
                                Toast.makeText(context, R.string.toast_downloading, Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(
                                    Uri.parse("file://" + localUri2),
                                    "application/vnd.android.package-archive");
                            context.startActivity(intent);
                            return;
                        }
                    }
                } finally {
                    if (c != null)
                        c.close();
                }
            }

            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(context.getString(R.string.app_name));
            request.setMimeType("application/vnd.android.package-archive");
            // request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS, "gocoro.apk");
            downloadId = downloadManager.enqueue(request);
            SPManager.setLong(context, SPManager.KEY_USER_APK_DOWNLOAD_ID, downloadId);

            return;
        } catch (Exception e) {
            Log.e(TAG, "downloadApp using DownloadManager fail.", e);
        }
    }
}
