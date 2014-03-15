package fi.nottingham.mobilefood;

import android.os.Bundle;
import android.support.v4.app.Fragment;

public class DaggerBaseFragment extends Fragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//((DaggerApplication) getActivity().getApplication()).inject(this);
	}
}
