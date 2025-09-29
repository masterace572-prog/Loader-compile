package pubgm.loader.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import pubgm.loader.adapter.MessageChatAdapter;
import pubgm.loader.adapter.MessageChatModel;
import pubgm.loader.ifc.Api;
import pubgm.loader.ifc.User;
import pubgm.loader.messaging.Client;
import pubgm.loader.utils.ActivityCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import io.michaelrocks.paranoid.Obfuscate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import pubgm.loader.R;

@Obfuscate
public class GroupActivity extends AppCompatActivity {
    FirebaseUser fuser = null;
    Api apiService;
    
    public static void goGroup(Context context) {
        Intent i = new Intent(context, GroupActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
    	context.startActivity(i);
    }
    
    List<MessageChatModel> messageChatModelList =  new ArrayList<>();
    RecyclerView recyclerView;
    MessageChatAdapter adapter ;

    EditText messageET;
    ImageView sendBtn;
    
    DatabaseReference referenceMessage;
    ValueEventListener seenListenerMessage;
    
    public void takeUsername() {
        EditText edit = new EditText(this);
        edit.setHint("....");
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg2.getFlags() == KeyEvent.FLAG_TRACKING) {
                    PreferenceManager.getDefaultSharedPreferences(GroupActivity.this).edit().putString("username", arg0.getText().toString());
                }
                return true;
            }
        });
        
        new MaterialAlertDialogBuilder(this, R.style.AppTheme)
            .setCancelable(false)
            .setTitle("Set Username")
            .setView(edit)
            .setPositiveButton(android.R.string.ok, (d, w) -> {
                ((TextView)findViewById(R.id.tv_username)).setText(PreferenceManager.getDefaultSharedPreferences(GroupActivity.this).getString("username", ""));
            }).show();
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler(this));
        setContentView(R.layout.activity_group);
        setNavBar(R.color.card_background);
        
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        
        apiService = Client.getClient("https://fcm.googleapis.com/").create(Api.class);
        
        TextView username = (TextView)findViewById(R.id.tv_username);
        messageET = (EditText)findViewById(R.id.messageET);
        sendBtn = (ImageView) findViewById(R.id.sendBtn);
        
        findViewById(R.id.back).setOnClickListener(v -> {
            MainActivity.goMain(this);
            finishActivity(0);
        });
        
        FirebaseDatabase.getInstance().getReference("users").child(fuser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                username.setText(user.getUsername());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                
            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(manager);



        readAllMessage();
        recyclerView.smoothScrollToPosition(messageChatModelList.size());
        adapter = new MessageChatAdapter(messageChatModelList, GroupActivity.this);
        recyclerView.setAdapter(adapter);
        
        /*recyclerView.smoothScrollToPosition(messageChatModelList.size());
        adapter = new MessageChatAdapter(messageChatModelList, this );
        recyclerView.setAdapter(adapter);*/


        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = messageET.getText().toString();
                String time = String.valueOf(System.currentTimeMillis());
                if (!msg.isEmpty()) {
                    sendMessage(username.getText().toString(), msg, time);
                }
/*
                MessageChatModel model = new MessageChatModel(
                        msg,
                        "10:00 PM",
                        0
                );
                messageChatModelList.add(model);
                recyclerView.smoothScrollToPosition(messageChatModelList.size());
                adapter.notifyDataSetChanged();
                messageET.setText("");
*/

            }
        });
    }
    
    @Override
    public void onBackPressed() {
        MainActivity.goMain(this);
        finishActivity(0);
    }
    
    public void setNavBar(int color){
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().setStatusBarColor(ContextCompat.getColor(this,color));
    }
    
    
    private void readAllMessage() {
        
        referenceMessage = FirebaseDatabase.getInstance().getReference("group_chats");
        seenListenerMessage = referenceMessage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageChatModelList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    MessageChatModel chat = snapshot.getValue(MessageChatModel.class);
                    messageChatModelList.add(chat);
                    recyclerView.smoothScrollToPosition(messageChatModelList.size());
                    adapter.notifyDataSetChanged();
                }
              //  recyclerView.smoothScrollToPosition(messageChatModelList.size());
               // adapter = new MessageChatAdapter(messageChatModelList, GroupActivity.this);
               // recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    
    private void sendMessage(String user, String msg, String time) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("uid", fuser.getUid());
        hashMap.put("user", user);
        hashMap.put("msg", msg);
        hashMap.put("time", time);
        hashMap.put("time", time);

        reference.child("group_chats").push().setValue(hashMap);
        
        
        referenceMessage = FirebaseDatabase.getInstance().getReference("group_chats");
        seenListenerMessage = referenceMessage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                messageChatModelList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    MessageChatModel chat = snapshot.getValue(MessageChatModel.class);
                    messageChatModelList.add(chat);
                    adapter.notifyDataSetChanged();
                    messageET.setText("");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    
    @Override
    protected void onPause() {
        referenceMessage.removeEventListener(seenListenerMessage);
        super.onPause();
    }
}
