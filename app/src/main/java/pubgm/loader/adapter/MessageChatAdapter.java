package pubgm.loader.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.molihuan.utilcode.util.ToastUtils;
import pubgm.loader.messaging.Data;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import io.michaelrocks.paranoid.Obfuscate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import pubgm.loader.R;

@Obfuscate
public class MessageChatAdapter extends RecyclerView.Adapter {
    FirebaseUser fuser;
    List<MessageChatModel> messageChatModelList;
    Context context;

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;


    public MessageChatAdapter(List<MessageChatModel> messageChatModelList, Context context) {
        this.messageChatModelList = messageChatModelList;
        this.context = context;
    }

    // Determines the appropriate ViewType according to the sender of the message.
    @Override
    public int getItemViewType(int position) {
        fuser = FirebaseAuth.getInstance().getCurrentUser();
        MessageChatModel message = (MessageChatModel) messageChatModelList.get(position);
        if (message.getUid().equals(fuser.getUid())) {
            // If the current user is the sender of the message
            Log.e("getItemViewType","0");
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            // If some other user sent the message
            Log.e("getItemViewType","1");
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.send_layout, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.receive_layout, parent, false);
            return new ReceivedMessageHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        MessageChatModel message = messageChatModelList.get(position);
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                ((SentMessageHolder) holder).bind(message);
                break;
            case VIEW_TYPE_MESSAGE_RECEIVED:
                ((ReceivedMessageHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return messageChatModelList.size();
    }


    private class SentMessageHolder extends RecyclerView.ViewHolder{

        TextView message;
        TextView time;


        public SentMessageHolder(@NonNull View itemView) {
            super(itemView);
            message = (TextView)itemView.findViewById(R.id.message);
            time = (TextView)itemView.findViewById(R.id.time);
            message.setOnLongClickListener(v -> {
                setClipboard(context, message.getText().toString());
                ToastUtils.showShort("Message copy");
                return true;
            });
        }

        void bind(MessageChatModel messageModel) {
            message.setText(messageModel.getMsg());
            time.setText(convertTime(messageModel.getTime()));
        }

    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder{
        TextView username;
        TextView message;
        TextView time;
        public ReceivedMessageHolder(@NonNull View itemView) {
            super(itemView);
            username = (TextView)itemView.findViewById(R.id.username);
            message = (TextView)itemView.findViewById(R.id.message);
            time = (TextView)itemView.findViewById(R.id.time);
            message.setOnLongClickListener(v -> {
                setClipboard(context, message.getText().toString());
                ToastUtils.showShort("Message copy");
                return true;
            });
        }

        void bind(MessageChatModel messageModel){
            username.setText(messageModel.getUser());
            message.setText(messageModel.getMsg());
            time.setText(convertTime(messageModel.getTime()));
        }
        
    }
    
    private void setClipboard(Context context, String text) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(text);
        } else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
            clipboard.setPrimaryClip(clip);
        }
    }
    
    public String convertTime(String time){
        SimpleDateFormat formatter = new SimpleDateFormat("h:mm a");
        String dateString = formatter.format(new Date(Long.parseLong(time)));
        return dateString;
    }
}
