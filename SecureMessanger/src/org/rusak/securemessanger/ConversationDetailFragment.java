package org.rusak.securemessanger;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.rusak.securemessanger.list.ConversationBriefData;

/**
 * A fragment representing a single Conversation detail screen. This fragment is
 * either contained in a {@link ConversationListActivity} in two-pane mode (on
 * tablets) or a {@link ConversationDetailActivity} on handsets.
 */
public class ConversationDetailFragment extends Fragment {
	private final String TAG = "ConversationDetailFragment";
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private ConversationBriefData conv;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ConversationDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			Log.v(TAG,"onCreate-ArgumentPassed: "+getArguments().getString(ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,	Bundle savedInstanceState) {
		//View rootView = inflater.inflate(R.layout.fragment_conversation_detail,	container, false);
		View rootView = inflater.inflate(R.layout.conversation_main_layout,	container, false);

		Log.v(TAG,"onCreateView");
		// Show the dummy content as text in a TextView.
		//if (mItem != null) {
			//((TextView) rootView.findViewById(R.id.conversation_detail)).setText("Conv detail");
		//}

		return rootView;
	}
}
