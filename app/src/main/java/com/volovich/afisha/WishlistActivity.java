package com.volovich.afisha;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.volovich.afisha.adapters.WishlistAdapter;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private String uid;
    private WishlistAdapter wishlistAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        setTitle(getResources().getString(R.string.wishlist));

        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        else uid = "";

        //initialize adapter with empty list of events
        this.wishlistAdapter = new WishlistAdapter(this);
        setRecyclerView();
        loadWishesFromFirestore();
    }

    private void loadWishesFromFirestore() {
        //load wishlist documents with uid == (current user uid) collection
        FirebaseFirestore.getInstance()
                .collection(getString(R.string.firestore_collection_wishlists))
                .whereEqualTo(getString(R.string.firestore_field_uid), uid)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() != null) {
                        if (task.getResult().size() == 0)
                            Snackbar.make(findViewById(R.id.wishlist_recycler_view), getString(R.string.you_havent_mark_any_event_yet), Snackbar.LENGTH_LONG).show();
                        List<Wishlist> wishlists = new ArrayList<>();
                        for (QueryDocumentSnapshot result : task.getResult()) {
                            Wishlist wishlist = result.toObject(Wishlist.class);
                            wishlist.setDocumentId(result.getId());
                            wishlists.add(wishlist);
                        }
                        //then load events documents by event id which we get from wishlists
                        for (final Wishlist wishlist : wishlists) {
                            FirebaseFirestore.getInstance()
                                    .collection(getString(R.string.firestore_collection_events))
                                    .document(wishlist.getEventId())
                                    .get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                @Override
                                public void onSuccess(DocumentSnapshot documentSnapshot) {
                                    Event event = documentSnapshot.toObject(Event.class);
                                    if (event != null) {
                                        //because we do not keep documentId as document field, we have to insert it like this
                                        event.setDocumentId(wishlist.getEventId());
                                        //for easy access between collections
                                        event.setWishListDocumentId(wishlist.getDocumentId());
                                        //for counting a total price
                                        event.setCount(wishlist.getCount());
                                        Log.d("logs", event.getWishListDocumentId());
                                        wishlistAdapter.addEvent(event);
                                        Log.d("logs", event.getTitle());
                                    }
                                }
                            });
                        }
                    }
                }
            }
        });
    }

    private void setRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.wishlist_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(wishlistAdapter);

        //for swiping
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                wishlistAdapter.removeEventFromWishlist(viewHolder.getAdapterPosition());
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present
        getMenuInflater().inflate(R.menu.menu_wishlist, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_open_afisha) {
            startAfishaActivity();
            return true;
        }

        if (id == R.id.action_sign_out) {
            signOut();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startAfishaActivity() {
        Intent intent = new Intent(WishlistActivity.this, AfishaActivity.class);
        startActivity(intent);
        finish();
    }


    private void signOut() {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        //start LoginActivity
                        Intent intent = new Intent(WishlistActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

}
