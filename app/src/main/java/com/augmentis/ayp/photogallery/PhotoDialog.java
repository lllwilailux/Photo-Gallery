package com.augmentis.ayp.photogallery;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import java.io.File;

/**
 * Created by Wilailux on 8/22/2016.
 */
public class PhotoDialog extends DialogFragment implements DialogInterface.OnClickListener {

    private File photoFile;
    private ImageView imageView;

    public static PhotoDialog newInstance (File file) {
        PhotoDialog pd = new PhotoDialog();
        Bundle args = new Bundle();
        args.putSerializable("PIC" , file);
        pd.setArguments(args);
        return pd;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        photoFile = (File) getArguments().getSerializable("PIC");
        //3.
        View v = LayoutInflater.from(getActivity()).inflate(R.layout.photo_dialog, null);

        imageView = (ImageView) v.findViewById(R.id.img_view);


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(v);
        builder.setPositiveButton(android.R.string.ok, this);

        return builder.create();
    }


    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        dialogInterface.dismiss();
    }

}
