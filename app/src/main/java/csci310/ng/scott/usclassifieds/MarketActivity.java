package csci310.ng.scott.usclassifieds;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import androidx.appcompat.widget.SearchView;

import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MarketActivity extends AppCompatActivity {

    private static final String TAG = "MarketActivity";
    public static final int REQUEST_CODE = 3;
    private int categoryIndex = 5;  // all categories
    private int sortIndex = 1;  // by price
    private int groupIndex = 0; // for items

    // Firebase stuff
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseAuth.AuthStateListener authStateListener = new FirebaseAuth.AuthStateListener() {
        @Override
        public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
            FirebaseUser currUser = firebaseAuth.getCurrentUser();
            if (currUser == null) {
                Toast.makeText(MarketActivity.this, "Signed Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), LandingActivity.class));
            }
        }
    };

    private ValueEventListener dbListener;

    // Array of items
    List<Item> itemsList = new ArrayList<>();
    List<User> userList = new ArrayList<>();

    // UI Elements
    // private TextView mTextMessage;
    private GridView gridViewItems;
    private FloatingActionButton fabAddItem;
    private Button buttonFilter;
    private Button buttonMap;
    private SearchView searchMarket;
    private User searchUser;
    private TextView results;

    private ItemAdapter adapter;
    private BottomNavigationView navigation;

    ListView list;
    ItemListAdapter itemAdapter;

    UserListAdapter userAdapter;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_market:
                    // mTextMessage.setText(R.string.title_market);
                    return true;
                case R.id.navigation_notifications:
                    // mTextMessage.setText(R.string.title_notifications);
                    Intent j = new Intent(MarketActivity.this, NotificationsActivity.class);
                    startActivity(j);
                    return true;
                case R.id.navigation_profile:
                    // mTextMessage.setText(R.string.title_profile);
                    Intent k = new Intent(MarketActivity.this, ProfileActivity.class);
                    startActivity(k);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Link bottom navigation
        // mTextMessage = (TextView) findViewById(R.id.message);
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        //set selected item depending on Activity
        navigation.setSelectedItemId(R.id.navigation_market);

        // Link UI elements
        //gridViewItems = findViewById(R.id.grid_items);
        fabAddItem = findViewById(R.id.fab_add_item);
        buttonFilter = findViewById(R.id.button_filter);
        buttonMap = findViewById(R.id.button_map);
        searchMarket = findViewById(R.id.search_market);
        results = findViewById(R.id.results);
        list = (ListView) findViewById(R.id.list_items);

        // Set FAB listener
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SellActivity.class);
                startActivityForResult(i, SellActivity.REQUEST_CODE);
            }
        });

        // Set map button listener
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent k = new Intent(getApplicationContext(), MapsActivity.class);
                Log.d(TAG, "trying to send items " + itemsList.size());
                k.putExtra("numItems", itemsList.size());
                for (int i=0;i<itemsList.size();i++){
                    k.putExtra("item" + i, itemsList.get(i));
                }
                startActivityForResult(k, MapsActivity.REQUEST_CODE);
            }
        });

        // Set filter button listener
        buttonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent j = new Intent(getApplicationContext(), FilterActivity.class);
                startActivityForResult(j, FilterActivity.REQUEST_CODE);
            }
        });

        searchMarket.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "Querying " + query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "New Query " + newText);
                displayFilteredResults(itemsList, userList, newText);
                return false;
            }
        });
        searchUser = null;
        // Get Intent from ProfileActivity SearchByUser
        if(getIntent() != null){
            Intent searchByUserIntent = getIntent();
            searchUser = (User) searchByUserIntent.getSerializableExtra("User");
            if(searchUser != null){
                Log.d(TAG, "It worked!!! " + searchUser.getFullName());
            }

        }



        final FirebaseUser currUser = firebaseAuth.getCurrentUser();
        DatabaseReference rootRef = firebaseDatabase.getReference();
        dbListener = rootRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                itemsList.clear();

                for(DataSnapshot dss : dataSnapshot.child("Item").getChildren()) {
                    Item item = dss.getValue(Item.class);
                    Log.d(TAG, "Item " + item.getItemID() + " is " + item.getTitle() + " sold by " + item.getSellerID());
                    itemsList.add(item);

                }

                for(DataSnapshot dss : dataSnapshot.child("User").getChildren()) {
                    User user = dss.getValue(User.class);
                    Log.d(TAG, "User " + user.getUserID() + " is " + user.getFullName() + " with email " + user.getEmail());
                    userList.add(user);
                }

                // Call "Filter" function to handle all filtering of listed results
                // including display between items/users

                displayFilteredResults(itemsList, userList, "");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void displayFilteredResults(final List<Item> itemsList, final List<User> userList, final String query) {

        // for items
        if (groupIndex == 0) {
            final User[] currUserInfo = new User[1];
            final FirebaseUser currUser = firebaseAuth.getCurrentUser();
            DatabaseReference rootRef = firebaseDatabase.getReference();
            rootRef.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    currUserInfo[0] = dataSnapshot.child("User").child(currUser.getUid()).getValue(User.class);

                    final List<Item> filteredList = new ArrayList<>();
                    for (Item item : itemsList) {
                        if (query.equals("")) {
                            if(searchUser != null) {
                                if(item.getSellerID().equals(searchUser.getUserID())) {
                                    filteredList.add(item);
                                }
                            } else {
                                if (categoryIndex == 5) {
                                    Log.d(TAG, "Filtered Item " + item.getItemID() + " is " + item.getTitle() + " sold by " + item.getSellerID());
                                    filteredList.add(item);
                                } else if (categoryIndex == 6) {



                                    Log.d(TAG,"currUser is : " + currUserInfo[0].getUserID() + " and seller: " + item.getSellerID());
                                    if(currUserInfo[0].isFriendsWith(item.getSellerID())) {
                                        Log.d(TAG, "Filtered Item " + item.getItemID() + " is " + item.getTitle() + " sold by " + item.getSellerID() + " of category " + categoryIndex);
                                        filteredList.add(item);
                                    }
                                } else if (item.getCategory() == categoryIndex) {
                                    Log.d(TAG, "Filtered Item " + item.getItemID() + " is " + item.getTitle() + " sold by " + item.getSellerID() + " of category " + categoryIndex);
                                    filteredList.add(item);
                                }
                            }
                        } else if (item.getTitle().toLowerCase().contains(query.toLowerCase())
                                || item.getDescription().toLowerCase().contains(query.toLowerCase())) {
                            if(searchUser != null) {
                                if(item.getSellerID().equals(searchUser.getUserID())) {
                                    filteredList.add(item);
                                }
                            } else {
                                if (categoryIndex == 5) {
                                    Log.d(TAG, "Filtered Item " + item.getItemID() + " is " + item.getTitle() + " sold by " + item.getSellerID());
                                    filteredList.add(item);
                                } else if (item.getCategory() == categoryIndex) {
                                    Log.d(TAG, "Filtered Item " + item.getItemID() + " is " + item.getTitle() + " sold by " + item.getSellerID() + " of category " + categoryIndex);
                                    filteredList.add(item);
                                }
                            }
                        }
                    }

                    if (sortIndex == 1) {
                        Collections.sort(filteredList, new Comparator<Item>() {
                            @Override
                            public int compare(Item i1, Item i2) {
                                return new Double(i1.getPrice()).compareTo(new Double(i2.getPrice()));
                            }
                        });
                    }

                    if(filteredList.size() == 0){
                        results.setVisibility(View.VISIBLE);
                    }
                    else{
                        results.setVisibility(View.INVISIBLE);
                    }
                    itemAdapter = new ItemListAdapter(getApplicationContext(), R.layout.layout_item, filteredList);
                    list.setAdapter(itemAdapter);

                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                            Intent intent = new Intent(MarketActivity.this, ViewItemActivity.class);
                            intent.putExtra("Item", filteredList.get(i));
                            startActivity(intent);
                        }
                    });
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        // for users
        else {
            final List<User> filteredUsers = new ArrayList<>();
            for (User user : userList) {
                if (query.equals("")) {
                    filteredUsers.add(user);
                } else if (user.getEmail().toLowerCase().contains(query.toLowerCase())
                        || user.getFullName().toLowerCase().contains(query.toLowerCase())
                        || user.getTextBio().toLowerCase().contains(query.toLowerCase())
                        || user.getPhone().toLowerCase().contains(query.toLowerCase())) {
                    filteredUsers.add(user);
                }
                Log.d(TAG, "Filtered User " + user.getUserID() + " is " + user.getFullName() + " with email " + user.getEmail());
            }
            if(filteredUsers.size() == 0){
                results.setVisibility(View.VISIBLE);
            }
            else{
                results.setVisibility(View.INVISIBLE);
            }
            userAdapter = new UserListAdapter(getApplicationContext(), R.layout.layout_user, filteredUsers);
            list.setAdapter(userAdapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(MarketActivity.this, OtherProfileActivity.class);
                    intent.putExtra("User", filteredUsers.get(i));
                    startActivity(intent);
                }
            });
        }

    }

    // Adapter for items
    private class ItemAdapter extends ArrayAdapter<Item> {
        private List<Item> items;

        public ItemAdapter(Context context, int resource, List<Item> objects){
            super(context, resource, objects);
            this.items = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_item, null);
            }

            Item curr = items.get(position);

            TextView textTitle = convertView.findViewById(R.id.list_item_name);
            TextView textPrice = convertView.findViewById(R.id.list_item_price);

            textTitle.setText(curr.getTitle());
            textPrice.setText(Double.toString(curr.getPrice()));

            return convertView;
        }

    }
    // Adapter for items
    private class UserAdapter extends ArrayAdapter<User> {
        private List<User> users;
        public UserAdapter(Context context, int resource, List<User> objects){
            super(context, resource, objects);
            this.users = objects;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_user, null);
            }
            User curr = users.get(position);
            TextView textFullName = convertView.findViewById(R.id.user_full_name);
            textFullName.setText(curr.getFullName());
            return convertView;
        }

    }

    // Not sure what this does
    @Override
    protected void onResume() {
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_market);
    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //on return from picking an image for profile
        if (requestCode== FilterActivity.REQUEST_CODE  && resultCode == RESULT_OK && data!=null) {
            searchUser = null;
            // get extra from intent that holds the user selected filter option
            int dataCat = data.getIntExtra("categoryIndex", -1);
            int dataSort = data.getIntExtra("sortIndex", -1);
            int dataGroup = data.getIntExtra("groupIndex", -1);

            categoryIndex = dataCat == -1 ? categoryIndex : dataCat;
            sortIndex = dataSort == -1 ? sortIndex : dataSort;
            groupIndex = dataGroup == -1 ? groupIndex : dataGroup;
            Log.d(TAG, "categoryIndex = " + categoryIndex + " sortIndex = " + sortIndex + " groupIndex = " + groupIndex);

            // update search results to display the filtered items
            displayFilteredResults(itemsList, userList, "");

        }

    }


}
