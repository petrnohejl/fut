package fut.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import fut.android.loop.Loop;

public class BootReceiver  extends BroadcastReceiver {

	private Intent intent;
	
	@Override
	public void onReceive(final Context context, final Intent bootintent) {
		intent = new Intent(context, Loop.class);
		context.startService(intent);
	}

}
