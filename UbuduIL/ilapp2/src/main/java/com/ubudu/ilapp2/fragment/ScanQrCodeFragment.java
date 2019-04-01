package com.ubudu.ilapp2.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.zxing.Result;
import com.ubudu.ilapp2.R;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQrCodeFragment extends BaseFragment implements ZXingScannerView.ResultHandler {

    public static final String TAG = ScanQrCodeFragment.class.getSimpleName();

    public ZXingScannerView mScannerView;

    private boolean QRdecoded = false;
    private boolean scannedQrCodeAccepted = false;

    public ScanQrCodeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_scan_qr_code, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mScannerView = (ZXingScannerView) getActivity().findViewById(R.id.qrdecoderview);
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
    }

    @Override
    public void onResume() {
        super.onResume();
        getViewController().scanQrCodeFragmentResumed();
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        mScannerView.stopCamera();

        getViewController().scanQrCodeFragmentPaused();

        if(!scannedQrCodeAccepted)
            getViewController().onNoQrCodeScannedOrAccepted();
        super.onPause();
    }

    @Override
    public void handleResult(Result result) {
        if(!QRdecoded) {
            QRdecoded = true;
            mScannerView.stopCamera();
            final String namespace = result.getText();
            final ZXingScannerView.ResultHandler resultHandler = this;
            Log.e(TAG, "decoded: " + namespace);
            new MaterialDialog.Builder(getActivity())
                    .content("Decoded QR code:\n " + namespace)
                    .cancelable(false)
                    .positiveText("OK")
                    .negativeText("Cancel")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            scannedQrCodeAccepted = true;
                            getViewController().onMapFragmentRequested();
                            getViewController().onNamespaceChanged(namespace);
                        }
                    })
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog dialog, DialogAction which) {
                            QRdecoded = false;
                            mScannerView.startCamera();
                            mScannerView.setResultHandler(resultHandler);
                        }
                    })
                    .show();
        }
    }
}
