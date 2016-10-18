package com.mecolab.memeticameandroid.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.mecolab.memeticameandroid.Models.User;
import com.mecolab.memeticameandroid.Persistence.ContactManager;
import com.mecolab.memeticameandroid.R;
import com.mecolab.memeticameandroid.Views.UserAdapter;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnItemClick;

import static com.google.android.gms.internal.zzid.runOnUiThread;

/**
 * Contacts Fragment
 */
public class ContactsFragment extends Fragment {

    @Bind(R.id.ContactsFragment_ContactsView)
    ListView mContactsView;

    private UserAdapter mAdapter;
    //private ArrayList<User> mPhoneContacts;
    private ArrayList<User> mRegisteredContacts;
    //private ContentResolver mResolver;
    private OnContactSelectedListener mListener;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_SECTION_NUMBER = "section_number";

    public interface OnContactSelectedListener {
        void onContactSelected(User contact);
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static ContactsFragment newInstance(int sectionNumber) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ContactsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, rootView);
        loadContacts();
        mAdapter = new UserAdapter(getActivity(), R.layout.user_list_item, mRegisteredContacts);
        mContactsView.setAdapter(mAdapter);
        return rootView;
    }

    @OnItemClick(R.id.ContactsFragment_ContactsView)
    public void onItemClick(int position){
        mListener.onContactSelected(mAdapter.getItem(position));
    }

    private void loadContacts() {
        mRegisteredContacts = User.getContacts(getActivity(),
                new ContactManager.ContactsProviderListener() {
                    @Override
                    public void OnContactsReady(ArrayList<User> users) {
                        for (User user : users) {
                            if (!mRegisteredContacts.contains(user)) {
                                mRegisteredContacts.add(user);
                                user.save(getActivity());
                            }
                        }
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    }
                });
    }

    public void refreshContacts() {
        loadContacts();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnContactSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }
}
