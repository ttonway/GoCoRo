package com.wcare.android.gocoro;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.wcare.android.gocoro.utils.SPManager;

public class DownloadCompleteReceiver extends BroadcastReceiver {
	private static final String TAG = DownloadCompleteReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive " + intent);
		if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
			long downloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if (downloadId != SPManager.getLong(context,
                    SPManager.KEY_USER_APK_DOWNLOAD_ID, -1)) {
				return;
			}

			DownloadManager downloadManager = (DownloadManager) context
					.getSystemService(Context.DOWNLOAD_SERVICE);
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(downloadId);
			query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);
			Cursor c = downloadManager.query(query);
			try {
				if (c != null && c.moveToFirst()) {
					String localUri = c.getString(c
							.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
					Intent intent2 = new Intent(Intent.ACTION_VIEW);
					intent2.setDataAndType(Uri.parse("file://" + localUri),
							"application/vnd.android.package-archive");
					intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intent2);
				}
			} finally {
				if (c != null)
					c.close();
			}
		}
	}
}
