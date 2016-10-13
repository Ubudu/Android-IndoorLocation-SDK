package com.ubudu.ilapp2.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import com.ubudu.ilapp2.util.ToastUtil;

import java.io.File;

public class BaseFragment extends Fragment {

    private ViewController mViewController;
    private FragmentActivity mActivity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = (FragmentActivity) context;

        try {
            mViewController = (ViewController) context;

        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement ViewController and UbuduInterface");
        }
    }

    public Activity getContextActivity() {
        return mActivity;
    }

    public ViewController getViewController() {
        return mViewController;
    }

    public interface ViewController {
        void onProgressDialogHideRequested();
        void onProgressDialogShowRequested(String text);

        void onMapFragmentRequested();
        void onRadarFragmentRequested();
        void onScanQrCodeFragmentRequested();
        void onSettingsFragmentRequested();

        void mapFragmentResumed();
        void radarFragmentResumed();
        void scanQrCodeFragmentResumed();
        void scanQrCodeFragmentPaused();

        void onDialogShowRequested(String title, String text, DialogResponseListener dialogResponseListener, boolean showNegative);

        void onNamespaceChanged(String namespace);

        void onNoQrCodeScannedOrAccepted();
    }

    public interface DialogResponseListener {
        void onPositive();
        void onNegative();
    }

    protected void shareText(String message) {
        if (message != null) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
            i.putExtra(Intent.EXTRA_SUBJECT, "Distance calibration coefficients");
            i.putExtra(Intent.EXTRA_TEXT, message);
            try {
                getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    protected void shareFile(File file) {
        if (file != null) {
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
            i.putExtra(Intent.EXTRA_SUBJECT, "");
            i.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            try {
                getActivity().startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        } else {
            ToastUtil.showToast(getContextActivity(),"File is null. Cannot share!");
        }
    }

}
