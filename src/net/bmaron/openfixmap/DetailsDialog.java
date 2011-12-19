package net.bmaron.openfixmap;

import java.util.Date;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DetailsDialog extends Dialog{ 
	protected ErrorItem item;
	protected ProgressDialog dialog;
	protected Boolean return_dialog;
	public DetailsDialog(Context context, ErrorItem item) {
		super(context);
		this.item = item;		
		requestWindowFeature(Window.FEATURE_NO_TITLE);//Remove Default Title
        setContentView(R.layout.errordetail_dialog);
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);

        TextView title = (TextView) findViewById(R.id.title);
        title.setText(item.getTitleOr(context.getResources().getString(R.string.details_bug_title)));

        TextView text = (TextView) findViewById(R.id.text);
        text.setText(item.getDescription());
        ImageView image = (ImageView) findViewById(R.id.image);

        image.setImageResource(item.getPlatform().getIcon());
        if(item.getErrorStatus() == ErrorItem.ST_CLOSE) {
    		CheckBox checkbox = (CheckBox) findViewById(R.id.detail_mark_as_close);
    		checkbox.setEnabled(false);
        }
        
        TextView status = (TextView) findViewById(R.id.status_txt);
        if(item.getErrorStatus() == ErrorItem.ST_CLOSE) {
        	status.setTextColor(Color.GREEN);
        	status.setText(context.getResources().getString(R.string.dialog_status_close));
        } else if(item.getErrorStatus() == ErrorItem.ST_OPEN) {
        	status.setTextColor(Color.RED);
        	status.setText(context.getResources().getString(R.string.dialog_status_open));
        } else if(item.getErrorStatus() == ErrorItem.ST_INVALID) {
        	status.setTextColor(Color.YELLOW);
        	status.setText(context.getResources().getString(R.string.dialog_status_invalid));
        }
        
        TextView parse_name = (TextView) findViewById(R.id.parser);
        parse_name.setText(item.getPlatform().getName());
        
        TextView error_date = (TextView) findViewById(R.id.error_date);
        java.text.DateFormat formDate= DateFormat.getMediumDateFormat(context);
        Date itemDate = item.getDate();
        if(itemDate == null)
        	itemDate = new Date();
        error_date.setText(formDate.format(itemDate));
        
        Button button = (Button) findViewById(R.id.close_button);
        
        
        button.setOnClickListener(new View.OnClickListener(){
        	
			@Override
			public void onClick(View v) {
				final CheckBox checkbox = (CheckBox) findViewById(R.id.detail_mark_as_close);
				//Close the bug if checked and not already closed 
				if(checkbox.isChecked() && DetailsDialog.this.item.getErrorStatus() != ErrorItem.ST_CLOSE) {
	                DetailsDialog.this.dismiss();

					dialog = ProgressDialog.show(DetailsDialog.this.getContext(), "", 
								DetailsDialog.this.getContext().getResources().getString(R.string.dialog_loading_message), true);
					new Thread() 
					{
					    public void run() { 

				    		DetailsDialog.this.item.setErrorStatus(ErrorItem.ST_CLOSE);
				    		return_dialog = DetailsDialog.this.item.getPlatform().closeError(DetailsDialog.this.item);

							DetailsDialog.this.getOwnerActivity().runOnUiThread(new Runnable() {
							    public void run() {
							    	DetailsDialog.this.dialog.dismiss();
					                if(return_dialog) {
					                	Toast toast = Toast.makeText(getContext(),
					                		getContext().getResources().getString(R.string.dialog_close_message),
					                		Toast.LENGTH_LONG);
					                	toast.show();
					                }
											
							    }
							});
					    }
					}.start(); 
				}else {
					dismiss();
				}
			}
        	
        });

        Button bFieldInfo = (Button) findViewById(R.id.note_info);
        bFieldInfo.setOnClickListener(new View.OnClickListener(){
        	
			@Override
			public void onClick(View v) {
		        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		        
		        emailIntent .setType("text/html");
		         
		        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
		        		Html.fromHtml("<a href=\""+DetailsDialog.this.item.getLink()+"\">"+DetailsDialog.this.item.getTitle()+"</a>"));
		        DetailsDialog.this.getContext().startActivity(Intent.createChooser(emailIntent, "Note OSM Error"));
		        //emailIntent .putExtra(android.content.Intent.EXTRA_SUBJECT, subject);

			}
        	
        });


	}

}
