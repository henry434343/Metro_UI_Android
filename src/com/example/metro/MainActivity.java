package com.example.metro;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.metro.ViewItem.ItemSize;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity implements View.OnTouchListener{

	private View _view;
	private ViewGroup _root;
	private int _xDelta;
	private int _yDelta;
	
	private int width;
	private int height;
	private int topBarHeight;
	
	private int itemWidth;
	private int itemHeight;
	
	private ArrayList<ViewItem> views;
	private ArrayList<Point> points;
	
	private Action action;
	private enum Action {
		reSize,
		move
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Display display = getWindowManager().getDefaultDisplay();
		android.graphics.Point size = new android.graphics.Point();
		display.getSize(size);
		width = size.x;
		height = size.y;
		topBarHeight = getTopBarHeight();
		itemWidth = width/4;
		itemHeight = height/6;
		_root = (ViewGroup)findViewById(R.id.root);
		views = new ArrayList<ViewItem>();
		initPoints();
	}
	
	private int getTopBarHeight() {
		int statusBar = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			statusBar = getResources().getDimensionPixelSize(resourceId);
		} 
	      
		int actionBar = 0;
		TypedValue tv = new TypedValue();
		if (getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true))
			actionBar = TypedValue.complexToDimensionPixelSize(tv.data,getResources().getDisplayMetrics());
		
		return statusBar+actionBar;
	}
	
	private void initPoints(){
		points = new ArrayList<Point>();
		for (int i = 0; i <= 3; i++) {
			for (int j = 0; j <= 4 ; j++) {
				Point p = new Point();
				p.X = i;
				p.Y = j;
				p.ckeck = false;
				points.add(p);
			}
		}
	}
	
	OnClickListener resize = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			action = Action.reSize;
			ViewItem item = views.get((Integer) v.getTag());
			itemResize(item);
		}
	};
	
	public boolean onTouch(View view, MotionEvent event) {
		action = Action.move;
	    final int X = (int) event.getRawX();
	    final int Y = (int) event.getRawY();
	    ViewItem nowItem = null;
    	for (ViewItem item : views) {
			if (item.view == view) {
				nowItem = item;
				break;
			}
		}
	    switch (event.getAction() & MotionEvent.ACTION_MASK) {
	        case MotionEvent.ACTION_DOWN:
	    	    RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
	            _xDelta = X - lParams.leftMargin;
	            _yDelta = Y - lParams.topMargin;
	            view.bringToFront();
	            break;
	        case MotionEvent.ACTION_UP:
	        	
	        	if (X/itemWidth > 3 || (Y-topBarHeight)/itemHeight > 4) {
					break;
				}
				RelativeLayout.LayoutParams layoutParamsup = (RelativeLayout.LayoutParams) view.getLayoutParams();	
				layoutParamsup.leftMargin = X/itemWidth * itemWidth;
				layoutParamsup.topMargin = (Y-topBarHeight)/itemHeight * itemHeight;
				view.setLayoutParams(layoutParamsup);
				setItemPosition(nowItem, new int[]{X/itemWidth,(Y-topBarHeight)/itemHeight});
				updateScreenPosition();
	            break;
	        case MotionEvent.ACTION_POINTER_DOWN:
	            break;
	        case MotionEvent.ACTION_POINTER_UP:
	            break;
	        case MotionEvent.ACTION_MOVE:
	  
	        	if (X/itemWidth > 3 || (Y-topBarHeight)/itemHeight > 4) {
					break;
				}
	        	
	        	if (nowItem.positions.get(0).X != X/itemWidth || nowItem.positions.get(0).Y != (Y-topBarHeight)/itemHeight) {
	        		setItemPosition(nowItem, new int[]{X/itemWidth,(Y-topBarHeight)/itemHeight});
	        		changePosition(nowItem,X/itemWidth,(Y-topBarHeight)/itemHeight);
				}
	        	
	            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();	        	
	        	if ( 0 <= X - _xDelta && X - _xDelta <= itemWidth*3) {
	        		layoutParams.leftMargin = X - _xDelta;
				}
	            
	        	if (0 <= Y - _yDelta && Y - _yDelta <= itemHeight*4) {
	        		layoutParams.topMargin = Y - _yDelta;
				}
	        	view.setLayoutParams(layoutParams);

	            break;
	    }  
	    _root.invalidate();
	    return true;
	}
	
	private void changePosition(ViewItem nowItem, int x, int y){
		chechOverlap(nowItem);	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// TODO Auto-generated method stub
		addViewItem();		
		return super.onMenuItemSelected(featureId, item);
	}
	
	private void addViewItem(){
		
		if (views.size() >= 20) {
			showToast("已達上限");
			return;
		}
		
	    LayoutInflater layoutInflater = LayoutInflater.from(this);  
	    _view = layoutInflater.inflate(R.layout.grid_item, null);
	    
	    TextView textView = (TextView)_view.findViewById(R.id.textView_grid);
	    textView.setText(""+ views.size());
	    
	    Button btn = (Button)_view.findViewById(R.id.resize);
	    btn.setOnClickListener(resize);
	    btn.setTag(views.size());
	    
	    int x = getNewViewPosition(ItemSize.min)[0];
	    int y = getNewViewPosition(ItemSize.min)[1];
	    
	    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(itemWidth, itemHeight);
	    layoutParams.leftMargin = (width/4)*x;
	    layoutParams.topMargin = (height/6)*y;
	    _view.setLayoutParams(layoutParams);

	    _view.setOnTouchListener(this); 
	    _root.addView(_view);
	    
	    
	    ViewItem viewItem = new ViewItem(new int[]{x,y});
	    viewItem.view = _view;
	    viewItem.size = ItemSize.min;
	    viewItem.tag = views.size();
	    views.add(viewItem);
	    updateScreenPosition();
	}
	
	private int[] getNewViewPosition(ItemSize size){
		switch (size) {
		case min:
			for (Point p : points) {
				if (!p.ckeck) 
					return new int[]{p.X,p.Y};
			}
			return new int[]{0,0};
		case mid_width:
			for (int i = 0; i < points.size(); i++) {
				try {
					if (!points.get(i).ckeck && !points.get(i+1).ckeck ) {
						if (points.get(i).X == 3) 
							continue;
						return new int[]{points.get(i).X,points.get(i).Y};
					}
				} catch (Exception e) {}
			}  
		case mid_height:
			for (int i = 0; i < points.size(); i++) {
				try {
					if (!points.get(i).ckeck && !points.get(i+4).ckeck) {
						if (points.get(i).Y == 4)
							continue;
						return new int[]{points.get(i).X,points.get(i).Y};
					}
				} catch (Exception e) {}  
			}
		case max:
			for (int i = 0; i < points.size(); i++) {
				try {
					if (!points.get(i).ckeck && !points.get(i+1).ckeck && !points.get(i+4).ckeck&& !points.get(i+5).ckeck) {
						if (points.get(i).X == 3 || points.get(i).Y == 4)
							continue;
						return new int[]{points.get(i).X,points.get(i).Y};
					}
				} catch (Exception e) {}
			}
		default:
			break;
		}
		return null;
	}
	
	private void updateScreenPosition(){

		initPoints();
		for (ViewItem item : views) {
			for (Point itemP : item.positions) {
				for (Point p : points) {
					if (p.isEqual(itemP)) {
						p.ckeck = true;
					}
				}
			}
		}
		
		Collections.sort(points,  new Comparator<Point>() {
			@Override
			public int compare(Point lhs, Point rhs) {
				// TODO Auto-generated method stub
				if (lhs.Y > rhs.Y) return 1;
				else if (lhs.Y < rhs.Y) return -1;
				else if (lhs.X > rhs.X) return 1;
				else return -1;
			}
		});
	}
	
	private void chechOverlap(ViewItem item){
		for (final ViewItem otherItem : views) {
			if (item != otherItem) {
				boolean isOverlap = false;
				for (Point p : item.positions) {
					for (Point otherP : otherItem.positions) {
						if (p.isEqual(otherP)) {
							isOverlap = true;
							final int[] start = getNewViewPosition(otherItem.size);
							if (start != null) {
								TranslateAnimation animation = new TranslateAnimation(0, (start[0] - otherItem.positions.get(0).X ) * itemWidth, 0, (start[1] - otherItem.positions.get(0).Y )*itemHeight);
								animation.setDuration(500);
								animation.setAnimationListener(new TranslateAnimation.AnimationListener() {
									
									@Override
									public void onAnimationStart(Animation animation) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onAnimationRepeat(Animation animation) {
										// TODO Auto-generated method stub
										
									}
									
									@Override
									public void onAnimationEnd(Animation animation) {
										// TODO Auto-generated method stub
										Log.i("chauster", "onAnimationEnd");
										completeAnimation(otherItem.view);
										RelativeLayout.LayoutParams layoutParamsup = (RelativeLayout.LayoutParams) otherItem.view.getLayoutParams();
										layoutParamsup.leftMargin = start[0] * itemWidth;
										layoutParamsup.topMargin = start[1] * itemHeight;
									}
								});
								setItemPosition(otherItem,new int[]{start[0],start[1]});
								updateScreenPosition();
								otherItem.view.startAnimation(animation);
								Log.i("chauster", "item "+otherItem.tag +" 需要被移動");
							}
							else {
								if (action == Action.reSize) {
									itemResize(item);
									showToast("無空間");
								}
								else {
									showToast("移動item碰到其他item，但無空間移動");
								}
							}
							break;
						}
					}

					if (isOverlap) {
						break;
					}
				}
			}
		} 
	}
	
	private void completeAnimation(View view){
		view.clearAnimation();
        view.setVisibility(View.GONE);
        view.setVisibility(View.VISIBLE);
	}
	
	private void itemResize(ViewItem item){
		int startX = item.positions.get(0).X;
		int startY = item.positions.get(0).Y;
		RelativeLayout.LayoutParams layoutParamsup = null;
		switch (item.size) {
		case min: 
			if (startX+1>3) {
				try {
					startX = getNewViewPosition(ItemSize.mid_width)[0];
					startY = getNewViewPosition(ItemSize.mid_width)[1];
				} catch (Exception e) {
					// TODO: handle exception
				}

			}
			layoutParamsup = new RelativeLayout.LayoutParams(itemWidth*2, itemHeight);	
			item.setPositions(new int[]{startX,startY},
							  new int[]{startX+1,startY});
		    item.size = ItemSize.mid_width;
			break;
			
		case mid_width: 
			if (startY+1>5) {
				try {
					startX = getNewViewPosition(ItemSize.mid_width)[0];
					startY = getNewViewPosition(ItemSize.mid_width)[1];
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			layoutParamsup = new RelativeLayout.LayoutParams(itemWidth, itemHeight*2);
			item.setPositions(new int[]{startX,startY},
							  new int[]{startX,startY+1});
		    item.size = ItemSize.mid_height;
			break;
			
		case mid_height:
			if (startX+1>3 || startY+1>5) {
				try {
					startX = getNewViewPosition(ItemSize.mid_width)[0];
					startY = getNewViewPosition(ItemSize.mid_width)[1];
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			layoutParamsup = new RelativeLayout.LayoutParams(itemWidth*2, itemHeight*2);
			item.setPositions(new int[]{startX,startY},
							  new int[]{startX+1,startY},
							  new int[]{startX,startY+1},
							  new int[]{startX+1,startY+1});
		    item.size = ItemSize.max;
			break;
			
		case max: 
			layoutParamsup = new RelativeLayout.LayoutParams(itemWidth, itemHeight);
			item.setPositions(new int[]{startX,startY});
		    item.size = ItemSize.min;
			break;

		default:
			break;
		}  
		updateScreenPosition();
		layoutParamsup.leftMargin = startX * itemWidth;
		layoutParamsup.topMargin = startY * itemHeight;
		item.view.setLayoutParams(layoutParamsup);
		chechOverlap(item);	
	}
	
	private void setItemPosition(ViewItem item, int[] start){
		switch (item.size) { 
		case min:
			item.positions.get(0).X = start[0];
			item.positions.get(0).Y = start[1];
			break;
		case mid_width:
			item.positions.get(0).X = start[0];
			item.positions.get(0).Y = start[1];
			
			item.positions.get(1).X = start[0]+1;
			item.positions.get(1).Y = start[1];
			break;
		case mid_height:
			item.positions.get(0).X = start[0];
			item.positions.get(0).Y = start[1];
			
			item.positions.get(1).X = start[0];
			item.positions.get(1).Y = start[1]+1;
			break;
		case max:
			item.positions.get(0).X = start[0];
			item.positions.get(0).Y = start[1];
			
			item.positions.get(1).X = start[0]+1;
			item.positions.get(1).Y = start[1];
			
			item.positions.get(2).X = start[0];
			item.positions.get(2).Y = start[1]+1;
			
			item.positions.get(3).X = start[0]+1;
			item.positions.get(3).Y = start[1]+1;
			break;

		default:
			break;
		}
		updateScreenPosition();
	}
	
	private void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

}