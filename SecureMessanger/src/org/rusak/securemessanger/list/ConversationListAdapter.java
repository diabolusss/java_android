package org.rusak.securemessanger.list;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import org.rusak.securemessanger.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ConversationListAdapter extends BaseAdapter {
	private Activity activity;
	private LayoutInflater inflater;
	private List<ConversationBriefData> conversationItems;

	public ConversationListAdapter(Activity activity, List<ConversationBriefData> ConversationItems) {
		this.activity = activity;
		this.conversationItems = ConversationItems;
	}

	@Override
	public int getCount() {
		return conversationItems.size();
	}

	@Override
	public Object getItem(int location) {
		return conversationItems.get(location);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		if (inflater == null)
			inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		if (convertView == null)
			convertView = inflater.inflate(R.layout.conversation_list_row, null);
		
		ImageView thumbNail = (ImageView) convertView.findViewById(R.id.thumbnail);
		TextView title = (TextView) convertView.findViewById(R.id.title);
		TextView msg_count = (TextView) convertView.findViewById(R.id.msg_count);
		TextView last_msg_cut = (TextView) convertView.findViewById(R.id.last_msg_cut);
		TextView timestamp = (TextView) convertView.findViewById(R.id.timestamp);

		// getting Conversation data for the row
		ConversationBriefData conv = conversationItems.get(position);

		// contact thumbnail image
		thumbNail.setImageResource(R.drawable.contact_thumbnail);
		
		// contaqct name
		title.setText(conv.getContactName());
		 
		// conversation message count
		msg_count.setText( Integer.toString( conv.getTotalMessageCount() ) );
		
		// last message cut\preview
		last_msg_cut.setText(conv.getLastMessageCut());
		
		// last msg timestamp
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(conv.getTimestamp());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		timestamp.setText(sdf.format(c.getTime()));

		return convertView;
	}

}