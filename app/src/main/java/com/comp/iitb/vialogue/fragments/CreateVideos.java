package com.comp.iitb.vialogue.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.coordinators.OnFragmentInteractionListener;
import com.comp.iitb.vialogue.coordinators.OnProgressUpdateListener;
import com.comp.iitb.vialogue.coordinators.SharedRuntimeContent;
import com.comp.iitb.vialogue.library.Storage;
import com.comp.iitb.vialogue.listeners.CameraImagePicker;
import com.comp.iitb.vialogue.listeners.ChangeVisibilityClick;
import com.comp.iitb.vialogue.listeners.ChangeVisibilityOnFocus;
import com.comp.iitb.vialogue.listeners.ClearFocusTouchListener;
import com.comp.iitb.vialogue.listeners.ImagePickerClick;
import com.comp.iitb.vialogue.listeners.ProjectTextWatcher;
import com.comp.iitb.vialogue.listeners.VideoPickerClick;

import java.io.File;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

import static com.comp.iitb.vialogue.coordinators.SharedRuntimeContent.GET_CAMERA_IMAGE;
import static com.comp.iitb.vialogue.coordinators.SharedRuntimeContent.GET_IMAGE;
import static com.comp.iitb.vialogue.coordinators.SharedRuntimeContent.GET_VIDEO;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CreateVideos#newInstance} factory method to
 * create an instance of this fragment.
 */
@RuntimePermissions
public class CreateVideos extends Fragment implements OnProgressUpdateListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Storage mStorage;
    private Button mImagePicker;
    private Button mVideoPicker;
    private Button mCameraPicker;
    private Button mQuestionPicker;
    private EditText mProjectName;
    private TextView mProjectNameDisplay;
    private SlideFragment mSlideFragment;
    private View mView;
    private OnFragmentInteractionListener mListener;
    private LinearLayout mRoot;
    private File mFolder;

    public CreateVideos() {

        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateVideos.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateVideos newInstance() {
        CreateVideos fragment = new CreateVideos();
        Bundle args = new Bundle();/*
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);*/
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_create_videos, container, false);
        //Initialize Storage
        mStorage = new Storage(getActivity());
        mProjectName = (EditText) mView.findViewById(R.id.project_name);
        mProjectNameDisplay = (TextView) mView.findViewById(R.id.project_name_display);
        mProjectNameDisplay.setOnClickListener(new ChangeVisibilityClick(getContext(), mProjectNameDisplay, mProjectName));
        mProjectName.setOnFocusChangeListener(new ChangeVisibilityOnFocus(mProjectName, mProjectNameDisplay));
        mRoot = (LinearLayout) mView.findViewById(R.id.create_videos_root);
        CreateVideosPermissionsDispatcher.setUpProjectWithCheck(this);

        //Load Pickers
        mImagePicker = (Button) mView.findViewById(R.id.image_picker);
        mVideoPicker = (Button) mView.findViewById(R.id.video_picker);
        mQuestionPicker = (Button) mView.findViewById(R.id.question_picker);
        mCameraPicker = (Button) mView.findViewById(R.id.camera_image_picker);

        CameraImagePicker cameraImagePicker = new CameraImagePicker(this);
        mCameraPicker.setOnClickListener(cameraImagePicker);

        //Image Picker
        ImagePickerClick imagePickerClickListener = new ImagePickerClick(this);
        mImagePicker.setOnClickListener(imagePickerClickListener);

        //Video Picker
        VideoPickerClick videoPickerClickListener = new VideoPickerClick(this);
        mVideoPicker.setOnClickListener(videoPickerClickListener);

        //set SlideLayout
        mSlideFragment = SlideFragment.newInstance(3);
        getFragmentManager().beginTransaction().add(R.id.create_videos_root, mSlideFragment).commit();

        //If User clicks at a place other than project Name EditText should convert to textView loosing focus
        FrameLayout touchInterceptor = (FrameLayout) mView.findViewById(R.id.touch_interceptor);
        touchInterceptor.setOnTouchListener(new ClearFocusTouchListener(mProjectName));

        return mView;
    }

    private void setEditTextLostFocus() {

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.d("Create Videos", "Visible");
            setUpProject();
        } else {
            Log.d("Create Videos", "Not visible");
        }
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void setUpProject() {
        mFolder = mStorage.getStorageDir(getString(R.string.create_project), true);
        mProjectNameDisplay.setText(getString(R.string.create_project));
        boolean success = true;
        if (mFolder != null) {
            if (!mFolder.exists()) {
                success = mFolder.mkdirs();
            }
            if (success) {
                mProjectName.addTextChangedListener(new ProjectTextWatcher(mStorage, mFolder, mProjectNameDisplay));
            } else {
                Snackbar.make(getView(), R.string.storage_error, Snackbar.LENGTH_LONG).show();
                mRoot.setEnabled(false);
            }
        }
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    public void OnPermissionDenied() {
        Snackbar.make(getView(), R.string.storage_error, Snackbar.LENGTH_LONG).show();
        mRoot.setVisibility(View.INVISIBLE);
    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForContact(PermissionRequest request) {
        // NOTE: Show a rationale to explain why the permission is needed, e.g. with a dialog.
        // Call proceed() or cancel() on the provided PermissionRequest to continue or abort
        showRationaleDialog(R.string.permission_storage_rationale, request);
    }

    private void showRationaleDialog(@StringRes int messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(getActivity())
                .setPositiveButton(R.string.button_allow, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();
                    }
                })
                .setNegativeButton(R.string.button_deny, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.cancel();
                    }
                })
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(1);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String selectedPath = null;
            if (requestCode == GET_CAMERA_IMAGE) {
                Bitmap photo = (Bitmap) data.getExtras().get("data");
                selectedPath = mStorage.getRealPathFromURI(mStorage.getImageUri(getContext(), photo));
            } else {
                selectedPath = mStorage.getRealPathFromURI(data.getData());
            }
            Log.d("Create Videos", "resultCode " + resultCode + " request code " + requestCode + " selectedPath " + selectedPath);
            File pickedFile = new File(selectedPath);
            switch (requestCode) {
                case GET_IMAGE:
                    if (!SharedRuntimeContent.imagePathList.contains(selectedPath)) {
                        mStorage.addFileToDirectory(mFolder, SharedRuntimeContent.IMAGE_FOLDER_NAME, pickedFile);
                        SharedRuntimeContent.imagePathList.add(pickedFile.getName());
                    }
                    break;
                case GET_VIDEO:
                    if (!SharedRuntimeContent.videoPathList.contains(selectedPath)) {
                        mStorage.addFileToDirectory(mFolder, SharedRuntimeContent.VIDEO_FOLDER_NAME, pickedFile);
                        SharedRuntimeContent.videoPathList.add(pickedFile.getName());
                    }
                    break;
                case GET_CAMERA_IMAGE:
                    if (!SharedRuntimeContent.imagePathList.contains(selectedPath)) {
                        mStorage.addFileToDirectory(mFolder, SharedRuntimeContent.IMAGE_FOLDER_NAME, pickedFile);
                        SharedRuntimeContent.imagePathList.add(pickedFile.getName());
                    }
                    break;

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // NOTE: delegate the permission handling to generated method
        CreateVideosPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    public void onProgressUpdate(int progress) {

    }
}