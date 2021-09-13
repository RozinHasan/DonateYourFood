package com.rozin.donateyourfood;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;
import com.rozin.donateyourfood.adapter.ChatAdapter;
import com.rozin.donateyourfood.adapter.ChatListAdapter;
import com.rozin.donateyourfood.models.Conversation;
import com.rozin.donateyourfood.models.Message;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.zhanghai.android.materialratingbar.MaterialRatingBar;

public class ChatActivity extends AppCompatActivity {
    // You know, this is used for logging purposes!
    static final String TAG = ChatActivity.class.getSimpleName();
    // This is the max number of messages to show
    static final int MAX_CHAT_MESSAGES_TO_SHOW = 100;

    static final String USER_ID_KEY = "userId";
    private static final String TOTAL_RATING_KEY = "numOfRatings";
    private static final String USERNAME = "username";

    // UI vars:
    EditText etxt_Message;
    Button btn_Send;
    RecyclerView rView_Messages;

    // RecyclerView vars:
    ArrayList<Message> rvMessages;
    ChatAdapter rvAdapter;

    String recieverName = "";
    String senderName = "";
    String postUniqueId = "";

    // Keep track of initial load to scroll to the bottom of the ListView
    boolean aFirstLoad;

    ProgressDialog mProgressDialog;


    /**
     * The Conversation list.
     */
    private ArrayList<Conversation> convList = new ArrayList();
    //private ArrayList<ParseObject> msgList;

    /**
     * The chat_layout adapter.
     */
    private ChatListAdapter mChatAdapter;
    /**
     * The date of last message in conversation.
     */
    private Date lastMsgDate;

    /**
     * Flag to hold if the activity is running or not.
     */
    private boolean isRunning;

    /**
     * The handler.
     */
    private static Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recieverName = getIntent().getStringExtra("RecieverId");
        postUniqueId = getIntent().getStringExtra("postObjectId");
        senderName = ParseUser.getCurrentUser().getUsername();
        handler = new Handler();
//
//        convList = new ArrayList<Conversation>();
//        ListView list = (ListView) findViewById(R.id.chatlist);
//
//        mChatAdapter = new ChatListAdapter(ChatActivity.this, senderName ,convList);
//        list.setAdapter(mChatAdapter);
////        list.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
//        list.setStackFromBottom(true);


        // Make sure the Parse server is setup to configured for live queries
        // URL for server is determined by Parse.initialize() call.
//        ParseLiveQueryClient parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient();
        // Init Live Query Client
        ParseLiveQueryClient parseLiveQueryClient = null;

        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI("wss://donateyourfood.back4app.io/"));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }


        ParseQuery<Message> parseQuery = ParseQuery.getQuery(Message.class);
        // This query can even be more granular (i.e. only refresh if the entry was added by some other user)
        parseQuery.whereNotEqualTo(USER_ID_KEY, ParseUser.getCurrentUser().getObjectId());

        // Connect to Parse server
        SubscriptionHandling<Message> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);
        subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<Message>() {
            @Override
            public void onEvent(ParseQuery<Message> query, Message object) {
//                Conversation c = new Conversation(object.getBody(), new Date(), recieverName);
//                c.setStatus(Conversation.STATUS_RECIEVED);
//                convList.add(0, c);
                rvMessages.add(0, object);

                // RecyclerView updates need to be run on the UI thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rvAdapter.notifyDataSetChanged();
                        rView_Messages.scrollToPosition(0);

//                        mChatAdapter.notifyDataSetChanged();
//                        list.smoothScrollToPosition(0);

                    }
                });
            }
        });


        // For now we will use the anonymous login, maybe later I update the app to use
        // accounts, but right now this is a basic and simple app
        if (ParseUser.getCurrentUser() != null) { // If we have already an user
            startWithCurrentUser(); // We start with it
        } else { // If not logged in, login as a new anonymous user
//            login();
        }

        refreshMessages(); // A quick workaround to have messages displayed when the activity is opened
//        loadConversationList();

    }

    // Function: Get the userId from the cached currentUser object
    void startWithCurrentUser() {
        setupMessagePosting();
    }

    // Function: Setup button event handler which posts the entered message to Parse
    void setupMessagePosting() {
        // Assign the UI vars
        etxt_Message = (EditText) findViewById(R.id.aC_etxt_Message);
        btn_Send = (Button) findViewById(R.id.aC_btn_Send);
        rView_Messages = (RecyclerView) findViewById(R.id.aC_rView_Messages);

        // Set up the vars used by the recycler view
        rvMessages = new ArrayList<>();
        final String userId = ParseUser.getCurrentUser().getObjectId();
        rvAdapter = new ChatAdapter(ChatActivity.this, userId, rvMessages);
        rView_Messages.setAdapter(rvAdapter);

        // And the vars used by the activity
        aFirstLoad = true;

        // We have to associate the LayoutManager with the RecyclerView
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChatActivity.this);
        rView_Messages.setLayoutManager(linearLayoutManager);
        linearLayoutManager.setReverseLayout(true); // This is a quick fix done to order messages from older to newer without doing a linear sort

        // Now let's configure what happens when the send button is clicked
        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // First we get the message (it is in the message edit text)
                String data = etxt_Message.getText().toString();
                if (data.length() == 0)
                    return;

//                final Conversation c = new Conversation(data, new Date(), senderName);
//                c.setStatus(Conversation.STATUS_SENDING);
//                convList.add(c);
//                mChatAdapter.notifyDataSetChanged();

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(etxt_Message.getWindowToken(), 0);


//                ParseObject postingdata = new ParseObject("ChatMessage");
//                postingdata.put("sender", senderName);
//                postingdata.put("reciever", recieverName);
//                postingdata.put("message", data);

                Message message = new Message();
                message.put("sender", senderName);
                message.put("reciever", recieverName);
                message.setUserId(ParseUser.getCurrentUser().getObjectId());
                message.put("chatroomName", senderName + recieverName + postUniqueId);
                message.put("message", data);
                message.setBody(data);
//
//                postingdata.saveInBackground(new SaveCallback() {
//                    @Override
//                    public void done(ParseException e) {
//                        if (e == null){
//                            Toast.makeText(ChatActivity.this, getString(R.string.toast_saved_message_ok), Toast.LENGTH_SHORT).show();
//                            c.setStatus(Conversation.STATUS_SENT);
//
//                        }else {
//                            Log.e(TAG, getString(R.string.toast_saved_message_err), e);
//                            c.setStatus(Conversation.STATUS_FAILED);
//                        }
//                        mChatAdapter.notifyDataSetChanged();
//
//                    }
//                });

//                // We will use the Message subclass we created
//                Message message=new Message();
//                // We assign the message the user has written
//                message.setBody(data);
//                // And we assign the user id we are using (right now it is an anonymous one, later maybe it will be a registered user)
//                message.setUserId(ParseUser.getCurrentUser().getObjectId());
//
                // Now we save the message (sending it to server if it is possible!)
                message.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
//                            Toast.makeText(ChatActivity.this, getString(R.string.toast_saved_message_ok),
//                                    Toast.LENGTH_SHORT).show();
                            refreshMessages();
                        } else {
                            Log.e(TAG, getString(R.string.toast_saved_message_err), e);
                        }
                    }
                });

                // And finally, just empty the input field to make the life of our loved user easier
                etxt_Message.setText(null);
            }
        });
    }

    // Function: Load the last 50 messages
    void refreshMessages() {
        // Construct query to execute
        ParseQuery<Message> query = ParseQuery.getQuery(Message.class);

//        ArrayList<String> al = new ArrayList<String>();
//        al.add(senderName+recieverName+postUniqueId);
//        query.whereContainsAll("chatroomName", al);
        query.whereEndsWith("chatroomName", postUniqueId);
//        query.whereExists("sender");
        query.setLimit(MAX_CHAT_MESSAGES_TO_SHOW);

        // Get the latest 50 messages, order will show up newest to oldest of this group
        query.orderByDescending("createdAt");
        // Execute query to fetch all messages from Parse asynchronously
        // This is equivalent to a SELECT query with SQL
        query.findInBackground(new FindCallback<Message>() {
            public void done(List<Message> messages, ParseException e) {
                if (e == null) {

                    if (messages.size() > 0) {
                        for (ParseObject msg : messages) {
                            String body = msg.getString("body");
                            Log.e("Body--->", body);
                        }
                    } else {
                        // No records found
                    }

                    // If there is no error, clear the Recycler View
                    rvMessages.clear();
                    // And add all the messages, updating the recycler view
                    rvMessages.addAll(messages);
                    rvAdapter.notifyDataSetChanged();
                    // Scroll to the bottom of the list on initial load
                    if (aFirstLoad) {
                        rView_Messages.scrollToPosition(0);
                        aFirstLoad = false;
                    }
                } else {
                    Log.e("message", getString(R.string.toast_load_messages_err) + e);
                    Toast.makeText(getApplicationContext(), getString(R.string.toast_load_messages_err), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Load the conversation list from Parse server and save the date of last
     * message that will be used to load only recent new messages
     */
    private void loadConversationList() {
        //	ParseQuery<ParseObject> senderquery = ParseQuery.getQuery("ChatActivity");
        //	ParseQuery<ParseObject> receivequery = ParseQuery.getQuery("ChatActivity");
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Message");
        if (convList.size() == 0) {
            // load all messages...
            ArrayList<String> al = new ArrayList<String>();
            al.add(recieverName);
            al.add(senderName);
            query.whereContainedIn("sender", al);
            query.whereContainedIn("receiver", al);
        } else {
            // load only newly received message..
            if (lastMsgDate != null)
                query.whereGreaterThan("createdAt", lastMsgDate);
            query.whereEqualTo("sender", recieverName);
            query.whereEqualTo("receiver", senderName);
        }
        //	if (lastMsgDate != null)
        //		query.whereGreaterThan("createdAt", lastMsgDate);
        query.orderByDescending("createdAt");
        query.setLimit(50);

        query.findInBackground(new FindCallback<ParseObject>() {

            @Override
            public void done(List<ParseObject> li, ParseException e) {
                if (li != null && li.size() > 0) {
                    Log.d("Rozin", "li size" + li.size());
                    for (int i = li.size() - 1; i >= 0; i--) {
                        ParseObject po = li.get(i);
                        Conversation c = new Conversation(po.getString("message"), po.getCreatedAt(),
                                po.getString("sender"));
                        convList.add(c);

                        Log.d("Rozin", "li size" + po.getString("message") + " " + po.getString("sender") + "");

                        if (lastMsgDate == null || lastMsgDate.before(c.getDate()))
                            lastMsgDate = c.getDate();
                        mChatAdapter.notifyDataSetChanged();
                    }
                }
                handler.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        if (isRunning)
                            loadConversationList();
                    }
                }, 1000);
            }
        });

    }

    // Function: Easy, create an anonymous user using ParseAnonymousUtils and set sUserId
    void login() {
        ParseAnonymousUtils.logIn(new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if (e != null) {
                    Log.e(TAG, getString(R.string.toast_anonymous_login_err), e);
                } else {
                    startWithCurrentUser();
                }
            }
        });
    }


    private void showRateDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.user_rating_view, null);

        MaterialRatingBar rateBar = (MaterialRatingBar) v.findViewById(R.id.ratingView);
        builder.setView(v);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
                Toast.makeText(context, String.valueOf(rateBar.getRating()), Toast.LENGTH_SHORT).show();

                switch ((int) rateBar.getRating()) {
                    case 1:
                        new RatingbarTask(context, "onestar", 1).execute();
                        break;
                    case 2:
                        new RatingbarTask(context, "twostar", 2).execute();
                        break;
                    case 3:
                        new RatingbarTask(context, "threestar", 3).execute();
                        break;
                    case 4:
                        new RatingbarTask(context, "fourstar", 4).execute();
                        break;
                    case 5:
                        new RatingbarTask(context, "fivestar", 5).execute();
                        break;
                    default:
                        break;
                }

            }
        });

        builder.create();
        builder.show();
    }


    private void showErrorDialog(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(error)
                .setCancelable(false)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        //Creating dialog box
        AlertDialog alert = builder.create();
        //Setting the title manually
        //alert.setTitle("AlertDialogExample");
        alert.show();
    }

    private class RatingbarTask extends AsyncTask<Void, Void, Void> {
        private String ratingkey;
        private int starcount;
        private Context context;

        RatingbarTask(Context context, String ratingkey, int starcount) {
            this.context = context;
            this.ratingkey = ratingkey;
            this.starcount = starcount;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // show the progressdialog
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setTitle("Please wait");
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.show();
        }


        @Override
        protected Void doInBackground(Void... voids) {
            updateRatings(recieverName, ratingkey, starcount);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // Close the progressdialog
//            mProgressDialog.dismiss();

        }
    }

    private void updateRatings(String recieverId, String ratingkey, int starcount) {

        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("UserRatings");
        query.whereEqualTo("userId", recieverId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    objects.get(0).increment(ratingkey, 1);
                    objects.get(0).increment(TOTAL_RATING_KEY);

                    objects.get(0).saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                /*calculate rating*/
                                int oneStar = objects.get(0).getInt("onestar");
                                int twoStar = objects.get(0).getInt("twostar");
                                int threeStar = objects.get(0).getInt("threestar");
                                int fourStar = objects.get(0).getInt("fourstar");
                                int fiveStar = objects.get(0).getInt("fivestar");


                                int rating = (1 * oneStar + 2 * twoStar + 3 * threeStar + 4 * fourStar + 5 * fiveStar) / (oneStar + twoStar + threeStar + fourStar + fiveStar);
                                givefoodUserRatings(recieverId, rating, starcount);

                            } else {
                                mProgressDialog.dismiss();
                                showErrorDialog(e.getLocalizedMessage());
                            }
                        }
                    });
                } else {
                    mProgressDialog.dismiss();
                    showErrorDialog(e.getLocalizedMessage());
                }
            }
        });
    }

    private void givefoodUserRatings(String userId, int rating, int starcount) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("foodtable");
        query.whereEqualTo("uid", userId);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects.size() > 0) {
                        objects.get(0).put("ratings", rating);
                        objects.get(0).saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    mProgressDialog.dismiss();
                                    showErrorDialog("Thanks for giving " + String.valueOf(starcount) + "star.");
                                } else {
                                    mProgressDialog.dismiss();
                                    showErrorDialog(e.getLocalizedMessage());
                                }
                            }
                        });
                    } else {
                        mProgressDialog.dismiss();
                        showErrorDialog("Error! \nThis user didn't contribute yet or no posts found for this user. Ask this user to make a food post first.");
                    }


                } else {
                    mProgressDialog.dismiss();
                    showErrorDialog(e.getLocalizedMessage());
                }
            }
        });
    }

//
//    public class RemoteDataTask extends AsyncTask<ParseQuery<ParseObject>, Void, List<Places>> {
//        @Override
//        protected List<Places> doInBackground(ParseQuery<ParseObject>... query) {
//            List<Places> places = new ArrayList<Places>();
//            try {
//                List<ParseObject> ob = query[0].find();
//                for (ParseObject place : ob) {
//                    ParseFile image = (ParseFile) place.get("image");
//                    Places p = new Places();
//                    p.setName((String) place.get("name"));
//                    p.setType((String) place.get("type"));
//                    p.setHours((String) place.get("hours"));
//                    p.setPhone((String)place.get("phone"));
//                    p.setDetails((String) place.get("details"));
//                    p.setImage(image.getUrl());
//                    places.add(p);
//                }
//            } catch (ParseException e) {
//                Log.e("Error", e.getMessage());
//                e.printStackTrace();
//            }
//            return places;
//        }
//    }


    @Override
    public void onBackPressed() {
        showRateDialog(this);
    }
}
