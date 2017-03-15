package com.comp.iitb.vialogue.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.comp.iitb.vialogue.MainActivity;
import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.adapters.QuestionAnswerDialog;
import com.comp.iitb.vialogue.coordinators.OnProjectSaved;
import com.comp.iitb.vialogue.coordinators.SharedRuntimeContent;
import com.comp.iitb.vialogue.dialogs.SingleOptionQuestion;
import com.comp.iitb.vialogue.library.SaveParseObjectAsync;
import com.comp.iitb.vialogue.models.CategoryType;
import com.comp.iitb.vialogue.models.ParseObjects.models.Category;
import com.comp.iitb.vialogue.models.ParseObjects.models.Language;
import com.comp.iitb.vialogue.models.QuestionAnswer;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import tcking.github.com.giraffeplayer.PlayerDialogAdapter;
import tcking.github.com.giraffeplayer.PlayerModel;
import tcking.github.com.giraffeplayer.SimulationHandler;
import tcking.github.com.giraffeplayer.VPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class UploadVideoActivity extends AppCompatActivity {

    public VPlayer mPlayer;
    List<Integer> time = new ArrayList<>();
    private boolean isFirstTime;
    private int goodToGo=0;
    private Spinner mCategories;
    private FloatingActionButton mUploadButton;
    public static String URL;
    private EditText name, description, language, tags;
    private final int MAX_WORD_LIMIT = 50;
    private List<QuestionAnswer> questionLists= new ArrayList();
    List<String> tagsToUpload = new ArrayList<>();
    List<String> categories = new ArrayList<>();
    List<ParseObject> receiveEM;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_video);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        URL=getIntent().getStringExtra("URL");
        Log.d("---size of questions",""+SharedRuntimeContent.questionsList.size());

        mPlayer = new VPlayer(this);

        questionLists = SharedRuntimeContent.questionsList;
       /* mUploadButton = (FloatingActionButton) findViewById(R.id.fab);
        mPlayer.play(new PlayerModel("http://"+URL, null));
        mPlayer.setTitle(URL);*/

        mUploadButton = (FloatingActionButton) findViewById(R.id.preview_fab);
        name= (EditText) findViewById(R.id.video_name);

        name.setFilters(new InputFilter[] { SharedRuntimeContent.filter });

        name.setText(SharedRuntimeContent.getProjectName());

        description= (EditText) findViewById(R.id.video_description);
        description.setFilters(new InputFilter[] { SharedRuntimeContent.filter });
        language= (EditText) findViewById(R.id.video_language);
        language.setFilters(new InputFilter[] { SharedRuntimeContent.filter });
        tags= (EditText) findViewById(R.id.video_tags);
        tags.setFilters(new InputFilter[] { SharedRuntimeContent.filter });
        mUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (name.getText().toString().trim().length() == 0) {
                    name.setError("Required");
                }
                if (description.getText().toString().trim().length() == 0) {
                    description.setError("Required");

                }
               /* if(mCategories.getSelectedItem().toString().equals("Select a category"))
                {
                    mCategories.set
                }*/

                if (language.getText().toString().trim().length() == 0) {
                    language.setError("Required");

                }
                if (tags.getText().toString().trim().length() == 0) {
                    tags.setError("Required");

                }

                /*
                else if (tags.getText().toString().trim().length() == 0) {
                    name.setError("Required");

                }*/
                else
                {

                if (ParseUser.getCurrentUser() == null) {
                    // User not signed in
                    Toast.makeText(UploadVideoActivity.this, R.string.signIn, Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(UploadVideoActivity.this, SignIn.class);
                    startActivity(intent);
                } else {
                    // User signed in, save project
                    SharedRuntimeContent.getProject().put("user", ParseUser.getCurrentUser());
                    SharedRuntimeContent.getProject().setName(name.getText().toString());
                    SharedRuntimeContent.getProject().setDescription(description.getText().toString());
                    Language lang = new Language(name.getText().toString());
                    SharedRuntimeContent.getProject().setLanguage(lang);
                    tagsToUpload= Arrays.asList(tags.getText().toString().split(" "));
                    SharedRuntimeContent.getProject().setTags(tagsToUpload);
                    new SaveParseObjectAsync(
                            UploadVideoActivity.this,
                            ProgressDialog.show(UploadVideoActivity.this, "Uploading Project", "Please wait...", true),
                            new OnProjectSaved() {
                                @Override
                                public void done(boolean isSaved) {
                                    if (isSaved) {
                                        Toast.makeText(UploadVideoActivity.this, R.string.projectSaved, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(UploadVideoActivity.this, R.string.couldntUpload, Toast.LENGTH_LONG).show();
                                    }
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                }
                            },
                            SharedRuntimeContent.getProject()
                    ).execute();
                }
            }


//                Intent intent = new Intent(getApplicationContext(), SignIn.class);
//                startActivity(intent);
            }
        });


        tags.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // Nothing
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String[] words = s.toString().split(" "); // Get all words
                if (words.length > MAX_WORD_LIMIT) {
                    return;
                }
            }
        });

        mPlayer.addPlayerDialogAdapter(new PlayerDialogAdapter() {
            private SimulationHandler mSimulationHandler;

            @Override
            public void bind(SimulationHandler simulationHandler) {
                mSimulationHandler = simulationHandler;
                isFirstTime = true;
            }

            @Override
            public void timeChanged(int currentPosition, boolean isUser) {
                Log.d("---is time changing","good question"+ currentPosition);
                QuestionAnswer questionAnswer = new QuestionAnswer();
                try{
                    if(questionLists.size()!=0)
                    {
                questionAnswer = questionLists.get(0);

                Log.d("time now",""+questionLists.get(0).getTime());
                if(currentPosition> questionLists.get(0).getTime()) {
                    popupQuestion(currentPosition, questionLists.get(0).getTime(), mSimulationHandler, questionAnswer);
                }
                }
                }
                catch(NullPointerException e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean seekToDifferentPosition(int currentPosition, int mediaIndex) {
                return false;
            }

            @Override
            public int moveTo(List<PlayerModel> playerModelList, int currentPosition, int mediaIndex) {
                Log.d(getClass().getName(), "timeChanged " + currentPosition + " is mediaIndex " + mediaIndex);
                return currentPosition;
            }
        });
        mPlayer.onComplete(new Runnable() {
            @Override
            public void run() {
                //callback when video is finish
                Toast.makeText(getApplicationContext(), R.string.videoCompleted, Toast.LENGTH_SHORT).show();
            }
        }).onInfo(new VPlayer.OnInfoListener() {
            @Override
            public void onInfo(int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        //do something when buffering start
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        //do something when buffering end
                        break;
                }
            }
        }).onError(new VPlayer.OnErrorListener() {
            @Override
            public void onError(int what, int extra) {
                Toast.makeText(getApplicationContext(), R.string.videoError, Toast.LENGTH_SHORT).show();
            }
        });
        new playHandler().execute();

        /*if(mPlayer.isPlaying())
        new DialogOpener().execute();*/
    }


    private class playHandler extends AsyncTask<String, String, String> {

        private String resp ="";
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            mPlayer.play(SharedRuntimeContent.getPreviewList());
            return resp;
        }


        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
        }


        @Override
        protected void onPreExecute() {

            progressDialog = ProgressDialog.show(UploadVideoActivity.this,
                    "ProgressDialog","Loading the preview");
            progressDialog.setCancelable(false);
        }


        @Override
        protected void onProgressUpdate(String... text) {

        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) {
            mPlayer.onPause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer != null) {
            mPlayer.onResume();
        }
        try {
            ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(
                    "CategoryType");
            query.orderByAscending("createdAt");
            receiveEM = query.find();
            categories.add("Select a category");
            for (ParseObject num : receiveEM) {
                String name=((String) num.get("name"));
                categories.add(name);
            }
        } catch (ParseException e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }

        mCategories = (Spinner) findViewById(R.id.category_choice);

        String[] plants = new String[categories.size()];
        plants = categories.toArray(plants);


        final List<String> plantsList = new ArrayList<>(Arrays.asList(plants));

        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this, R.layout.category_option_view, plantsList) {
            @Override
            public boolean isEnabled(int position) {

                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                if (position == 0) {
                    // Set the hint text color gray
                    tv.setTextColor(Color.GRAY);
                } else {
                    tv.setTextColor(Color.BLACK);
                }
                return view;
            }
        };/*
        spinnerArrayAdapter.setDropDownViewResource(R.layout.category_option_view);*/
        mCategories.setAdapter(spinnerArrayAdapter);

        mCategories.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItemText = (String) parent.getItemAtPosition(position);
                // If user change the default selection
                // First item is disable and it is used for hint
                if (position > 0) {
                    // Notify the selected item text
                    Toast.makeText
                            (getApplicationContext(), getResources().getString(R.string.selected) + selectedItemText, Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mPlayer != null) {
            mPlayer.onConfigurationChanged(newConfig);
        }
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mUploadButton.hide();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mUploadButton.show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mPlayer != null && mPlayer.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    public void popupQuestion(int currentPosition,long intendedPosition, SimulationHandler mSimulationHandler, QuestionAnswer question)
    {
        if (currentPosition > intendedPosition  && currentPosition<intendedPosition+500) {
            mPlayer.pause();

        /*questionAnswer.setIsCompulsory(false);*/
            QuestionAnswerDialog adapter = new SingleOptionQuestion(mSimulationHandler.getActivity(), question);
            adapter.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mPlayer.start();
                    Log.d("popped time",""+questionLists.remove(0));

                }
            });
            adapter.show();
        }
    }

}
