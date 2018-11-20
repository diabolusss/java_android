package org.rusak.securemessanger;

import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.rusak.securemessanger.constants.SMSConstants;
import org.rusak.securemessanger.list.ConversationBriefData;
import org.rusak.securemessanger.list.ConversationListAdapter;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A list fragment representing a list of Conversations. This fragment also
 * supports tablet devices by allowing list items to be given an 'activated'
 * state upon selection. This helps indicate which item is currently being
 * viewed in a {@link ConversationDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ConversationListFragment extends ListFragment {
	
	private final String TAG = "ConversationListFragment";

	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	public List<ConversationBriefData> conversationList = new ArrayList<ConversationBriefData>();

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ConversationListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		
		//setup list adapter.
		ConversationListAdapter conversationListAdapter = new ConversationListAdapter(this.getActivity(), conversationList);
		this.setListAdapter(conversationListAdapter);
		
		//add data
		conversationList.addAll(getConversationsBriefData());
		
		// notifying list adapter about data changes
		// so that it renders the list view with updated data
		conversationListAdapter.notifyDataSetChanged();

	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onListItemClick(ListView listView, View view, int position,long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		mCallbacks.onItemSelected(conversationList.get(position).getContactName());
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE : ListView.CHOICE_MODE_NONE
		);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	private List<ConversationBriefData> getConversationsBriefData(){
		List<ConversationBriefData> tmpList = new ArrayList<ConversationBriefData>();
		
		ContentResolver contentResolver = this.getActivity().getContentResolver();
		//Query() compared to SQL query. 
		//query() argument	|	SELECT keyword/parameter	|	Notes
		//Uri				|FROM table_name				|Uri maps to the table in the provider named table_name.
		//projection		|col,col,col,...				|projection is an array of columns that should be included for each row retrieved. 
		//selection			|WHERE col = value				|selection specifies the criteria for selecting rows.
		//selectionArgs	 	|(No exact equivalent. Selection arguments replace ? placeholders in the selection clause.) 
		//sortOrder			|ORDER BY col,col,...			|sortOrder specifies the order in which rows appear in the returned Cursor.
		Cursor cursor = contentResolver.query( Uri.parse( SMSConstants.STORAGE_URI ), 
				new String[]{
					SMSConstants.THREAD_ID, //unique conversation id
					SMSConstants.DATE,		//timestamp
					SMSConstants.ADDRESS	//contact phone number(what if its mail!?)
				},
				//null, 
				null, null, 
				SMSConstants.THREAD_ID+" ASC,"+
				//SMSConstants.ADDRESS+","+
				SMSConstants.DATE+" DESC"
				+","+SMSConstants.ADDRESS
		);

		int conversationIDIndex		 	= cursor.getColumnIndex( SMSConstants.THREAD_ID );
		int conversationContactAddrIndex	= cursor.getColumnIndex( SMSConstants.ADDRESS );
		int conversationMsgTimestampIndex = cursor.getColumnIndex( SMSConstants.DATE );
		
		if ( !cursor.moveToFirst() ) return null;
			
		ConversationBriefData conv;
		String contactAddress = null;
		long timestamp = 0L;
		int threadID = 0;
		do {
			Log.v(TAG,"$$$$BEGIN$$$$ STORED MSG DATA $$$$$$$$$$");
			//for(int i=0; i < cursor.getColumnCount(); i ++){
			//	String str = cursor.getString(i);
			//	Log.v(TAG,"\t["+i+"]["+((str!=null)?(str.length()):(0))+"]"+cursor.getColumnName(i)+"="+str);				
			//}
						
			String currContactAddress = cursor.getString(conversationContactAddrIndex);
			long currTimestamp = cursor.getLong(conversationMsgTimestampIndex);
			int currThreadID = cursor.getInt(conversationIDIndex);
			
			//if checking new conversation thread create object to store data
			if(threadID != currThreadID){
				conv = new ConversationBriefData();
			}
			
			Log.v(TAG,"$$$$END$$$$ STORED MSG DATA $$$$$$$$$$");
		} while( cursor.moveToNext() );

		cursor.close();
		
		for (int i = 0; i < 10; i++) {
			ConversationBriefData conv = new ConversationBriefData();
			conv.setContactName("Contact Name"+i);
			conv.setThumbnail("thumbnailURL"+i);
			conv.setFirstMsgID(i);
			conv.setTotalMessageCount(i);
			conv.setLastMessageCut("Some message part preview...");
			conv.setTimestamp(System.currentTimeMillis());
			
			tmpList.add(conv);				
		}
		
		return tmpList;		
	}

}
