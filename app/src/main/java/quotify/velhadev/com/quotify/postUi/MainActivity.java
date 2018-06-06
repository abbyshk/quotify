package quotify.velhadev.com.quotify.postUi;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import quotify.velhadev.com.quotify.widget.QuotifyWidget;
import quotify.velhadev.com.quotify.R;
import quotify.velhadev.com.quotify.Utils.Tags;
import quotify.velhadev.com.quotify.widget.UpdateQuoteService;
import quotify.velhadev.com.quotify.imageViewer.ImageViewer;


public class MainActivity extends AppCompatActivity implements PostsViewAdapter.PostsOnclickListener, PersonalPostsViewAdapter.PersonalPostOnClickListener{

    private static final int RC_SIGN_IN = 1;

    private float slidingOffset = 0f;

    private ProgressBar progressBar;
    private LinearLayout emptyView;
    private FloatingActionButton emptyViewAddButton;
    private TextView usernameTextView;
    private ImageView userImageView;
    private SlidingUpPanelLayout slidingPanel;
    private TextView profileHeaderTitle;
    private RelativeLayout dragView;
    private FloatingActionButton createQuote;
    private Button signOutButton;
    private PostsViewAdapter adapter;
    private RecyclerView personalCollectionRecyclerView;
    private PersonalPostsViewAdapter personalCollectionAdapter;
    private TextView emptyTextView;

    private AppWidgetManager appWidgetManager;
    private int[] appWidgetIds;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;


    private SlidingUpPanelLayout.PanelSlideListener panelStateChangeListener = new SlidingUpPanelLayout.PanelSlideListener() {
        @Override
        public void onPanelSlide(View panel, float slideOffset) {
            slidingOffset = slideOffset;
            changeAlphaValues();
        }

        @Override
        public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        }
    };

    private View.OnClickListener createQuoteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getApplicationContext(), WriteQuoteActivity.class);
            startActivity(intent);
        }
    };

    private View.OnClickListener signOutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            AuthUI.getInstance().signOut(getApplicationContext());
            QuotifyWidget.updateQuotifyWidget(getApplicationContext(), appWidgetManager, appWidgetIds, null);
            clearData();
            clearAuthCredentials();
        }
    };

    private ValueEventListener valueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(!dataSnapshot.exists()){
                progressBar.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private ValueEventListener personalPostValueEventListener = new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            if(!dataSnapshot.exists()){
                emptyTextView.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
    };

    private void clearData() {
        if(adapter != null) adapter.clear();
        if(personalCollectionAdapter != null) personalCollectionAdapter.clear();
        if(emptyView != null) emptyView.setVisibility(View.GONE);
        if(emptyTextView != null) emptyTextView.setVisibility(View.GONE);
    }

    private void changeAlphaValues() {
        usernameTextView.setAlpha(slidingOffset);
        userImageView.setAlpha(slidingOffset);
        personalCollectionRecyclerView.setAlpha(slidingOffset);
        dragView.getChildAt(1).setAlpha(slidingOffset);
        dragView.getChildAt(2).setAlpha(1 - slidingOffset);
        profileHeaderTitle.setAlpha(1 - slidingOffset);
        createQuote.setAlpha(slidingOffset);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MultiDex.install(this);
        appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, QuotifyWidget.class));
        handleAuthentication();
    }

    private void clearAuthCredentials() {
        if(slidingPanel != null) slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        if(usernameTextView != null) usernameTextView.setText("");
        if(userImageView != null) userImageView.setImageBitmap(null);
        if(profileHeaderTitle != null) profileHeaderTitle.setText("");
    }

    private void handleAuthentication() {

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    startMainScreen();
                } else {
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setTheme(R.style.CustomLoginTheme)
                                    .setLogo(R.drawable.logo)
                                    .setAvailableProviders(Arrays.asList(
                                            new AuthUI.IdpConfig.EmailBuilder().build(),
                                            new AuthUI.IdpConfig.GoogleBuilder().build()))
                                    .build(),
                            RC_SIGN_IN);
                }
            }
        };


    }

    private void startMainScreen() {
        if(user == null) return;
        initializeViews();
        initializeListeners();
        setUsername(user.getDisplayName());
        setUserImage(user.getPhotoUrl());
        getData();
    }

    private void getData() {
        final DatabaseReference allPostRef = FirebaseDatabase.getInstance().getReference().child("all_posts");

        allPostRef.addListenerForSingleValueEvent(valueEventListener);

        allPostRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                allPostRef.removeEventListener(valueEventListener);
                progressBar.setVisibility(View.GONE);
                Post data = dataSnapshot.getValue(Post.class);
                adapter.addData(data);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                clearData();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                clearData();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                clearData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        final DatabaseReference personalPostsRef = FirebaseDatabase.getInstance().getReference().child("personal_posts/" + user.getUid());

        personalPostsRef.addValueEventListener(personalPostValueEventListener);

        personalPostsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                personalPostsRef.removeEventListener(personalPostValueEventListener);
                PersonalPost data = dataSnapshot.getValue(PersonalPost.class);
                personalCollectionAdapter.addData(data);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                clearData();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                clearData();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                clearData();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                clearData();
            }
        });
    }

    private void initializeListeners() {
        slidingPanel.addPanelSlideListener(panelStateChangeListener);
        createQuote.setOnClickListener(createQuoteListener);
        signOutButton.setOnClickListener(signOutListener);
        emptyViewAddButton.setOnClickListener(createQuoteListener);
    }

    private void initializeViews() {
        usernameTextView = findViewById(R.id.profile_user_name);
        userImageView = findViewById(R.id.profile_user_image);
        slidingPanel = findViewById(R.id.sliding_layout);
        profileHeaderTitle = findViewById(R.id.profile_header);
        dragView = findViewById(R.id.drag_view);
        slidingPanel.setDragView(dragView);
        signOutButton = findViewById(R.id.sign_out_btn);
        createQuote = findViewById(R.id.btn_create_quote);
        progressBar = findViewById(R.id.progress_bar);
        emptyView = findViewById(R.id.empty_view_main);
        emptyViewAddButton = (FloatingActionButton) emptyView.getChildAt(1);
        emptyTextView = findViewById(R.id.empty_collection);
        setUpRecyclerView();
    }

    private void setUpRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.rv_main);
        adapter = new PostsViewAdapter(this);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, calculateNumberOfColumns());
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.setAdapter(adapter);

        personalCollectionRecyclerView = findViewById(R.id.rv_personal_collection);
        personalCollectionAdapter = new PersonalPostsViewAdapter(this);
        GridLayoutManager personalCollectionGridLayoutManager = new GridLayoutManager(this, 3);
        personalCollectionRecyclerView.setLayoutManager(personalCollectionGridLayoutManager);
        personalCollectionRecyclerView.setAdapter(personalCollectionAdapter);
    }

    private void setUserImage(Uri photoUrl) {
        Glide.with(this)
                .load(photoUrl)
                .apply(RequestOptions.circleCropTransform()).into(userImageView);
    }

    private void setUsername(String displayName) {
        profileHeaderTitle.setText(displayName);
        usernameTextView.setText(displayName);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                UpdateQuoteService.refreshWidgetData(getApplicationContext());
            }
            else if(resultCode == RESULT_CANCELED)
                finish();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(authStateListener != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
            clearData();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat(Tags.ALPHA_VALUE, slidingOffset);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        slidingOffset = savedInstanceState.getFloat(Tags.ALPHA_VALUE);
        user = firebaseAuth.getCurrentUser();
        if(user != null) {
            clearData();
            startMainScreen();
            changeAlphaValues();
        }
    }

    public int calculateNumberOfColumns() {

        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

        float displayWidth = displayMetrics.widthPixels / displayMetrics.density;

        int numberOfColumns = (int) displayWidth / 180;

        return numberOfColumns > 2 ? numberOfColumns : 1;
    }

    @Override
    public void onPostClick(int position, List<Post> data) {
        Intent intent = new Intent(this, ImageViewer.class);
        intent.putParcelableArrayListExtra(Tags.POST_DATA, (ArrayList<Post>)data);
        intent.putExtra(Tags.POST_POSITION, position);
        startActivity(intent);
    }

    @Override
    public void onPersonalPostClick(int position, List<PersonalPost> data) {
        Intent intent = new Intent(this, ImageViewer.class);
        intent.putParcelableArrayListExtra(Tags.POST_DATA, (ArrayList<PersonalPost>)data);
        intent.putExtra(Tags.POST_POSITION, position);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        if(slidingPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            slidingPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
        else
            finish();
    }
}
