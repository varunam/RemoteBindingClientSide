package app.remotebindingclient.com.remotebindingclientside;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    
    private static final String TAG = MainActivity.class.getSimpleName();
    
    private Button bindServiceButton, unbindServiceButton, getRandomNumberButton;
    private TextView randomNumberTextView;
    
    private Intent serviceIntent;
    private boolean isServiceBound;
    private int randomNumber;
    
    private Messenger randomNumberRequestMessenger, randomNumberReceiveMessenger;
    private static final int GET_RANDOM_NUMBER = 0;
    
    private ServiceConnection randomNumberServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            randomNumberRequestMessenger = new Messenger(iBinder);
            randomNumberReceiveMessenger = new Messenger(new RandomNumberReceiveHandler());
            Log.e(TAG,"Service connected");
            isServiceBound = true;
        }
        
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
            Log.e(TAG,"Service disconnected");
            randomNumberRequestMessenger = null;
            randomNumberReceiveMessenger = null;
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        bindServiceButton = findViewById(R.id.bind_service_button_id);
        unbindServiceButton = findViewById(R.id.unbind_service_button_id);
        getRandomNumberButton = findViewById(R.id.get_random_number_button_id);
        randomNumberTextView = findViewById(R.id.random_number_textview_id);
        
        bindServiceButton.setOnClickListener(this);
        unbindServiceButton.setOnClickListener(this);
        getRandomNumberButton.setOnClickListener(this);
        
        serviceIntent = new Intent();
        serviceIntent.setComponent(new ComponentName("app.remotebindingserver.com.remotebindingserver", "app.remotebindingserver.com.remotebindingserver.BoundService"));
    }
    
    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.get_random_number_button_id:
                fetchRandomNumber();
                break;
            case R.id.unbind_service_button_id:
                unbindRemoteServicex();
                break;
            case R.id.bind_service_button_id:
                bindToRemoteService();
                break;
        }
    }
    
    @Override
    protected void onDestroy() {
        randomNumberServiceConnection = null;
        super.onDestroy();
    }
    
    private void fetchRandomNumber() {
        if (isServiceBound) {
            Message randomNumberRequestMessage = Message.obtain(null, GET_RANDOM_NUMBER);
            randomNumberRequestMessage.replyTo = randomNumberReceiveMessenger;
            try {
                randomNumberRequestMessenger.send(randomNumberRequestMessage);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else
            randomNumberTextView.setText(getString(R.string.service_not_bound));
    }
    
    private void unbindRemoteServicex() {
        isServiceBound = false;
        unbindService(randomNumberServiceConnection);
        Toast.makeText(getApplicationContext(), "Service unbound", Toast.LENGTH_LONG).show();
    }
    
    private void bindToRemoteService() {
        Log.e(TAG, "Activity bound to remote service");
        Toast.makeText(getApplicationContext(), "Service bound", Toast.LENGTH_LONG).show();
        bindService(serviceIntent, randomNumberServiceConnection, BIND_AUTO_CREATE);
    }
    
    private class RandomNumberReceiveHandler extends Handler {
        
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GET_RANDOM_NUMBER:
                    randomNumber = msg.arg1;
                    String randomNumberString = getString(R.string.random_number) + " " + randomNumber;
                    randomNumberTextView.setText(randomNumberString);
                    break;
                default:
                    break;
            }
        }
    }
}
