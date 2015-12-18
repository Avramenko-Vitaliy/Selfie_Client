package com.deadpeace.selfie.util;

import com.deadpeace.selfie.model.Selfie;
import com.deadpeace.selfie.model.User;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.*;
import retrofit.mime.TypedFile;

import java.util.List;

/**
 * Created by Виталий on 06.10.2015.
 */

public interface SelfieSvcApi
{
    String ID_PARAM="id";
    String USER_NAME_PARAM="username";
    String PASSWORD_PARAM="password";
    String EMAIL_PARAM="email";
    String FILE_PARAM="file";
    String DEGREE_PARAM="degree";
    String BRIGHTNESS_PARAM="bright";
    String DEEP_PARAM="deep";

    String SVC_LOGIN="/login";
    String SVC_LOGOUT="/logout";
    String SVC_REGISTER="/register";
    String SVC_SELFIE="/selfie";
    String SVC_USER="/user";
    String SVC_SELFIE_ID=SVC_SELFIE+"/{"+ID_PARAM+"}";
    String SVC_SELFIE_UPLOAD=SVC_SELFIE_ID+"/upload";
    String SVC_SELFIE_DOWNLOAD=SVC_SELFIE_ID+"/download";
    String SVC_SELFIE_DOWNLOAD_PREVIEW=SVC_SELFIE_ID+"/download/preview";
    String SVC_SELFIE_LIKE=SVC_SELFIE_ID+"/like";
    String SVC_AVATAR_UPLOAD=SVC_USER+"/upload";
    String SVC_AVATAR_DOWNLOAD=SVC_USER+"/{"+USER_NAME_PARAM+"}"+"/download";
    String SVC_AVATAR_DOWNLOAD_PREVIEW=SVC_USER+"/{"+USER_NAME_PARAM+"}/download/preview";
    String SVC_SELFIE_ROTATE="/rotate/{"+DEGREE_PARAM+"}";
    String SVC_SELFIE_SEPIA="/sepia/{"+DEEP_PARAM+"}";
    String SVC_SELFIE_BLACK_AND_WHITE="/black_and_white";
    String SVC_SELFIE_BRIGHTNESS="/brightness/{"+BRIGHTNESS_PARAM+"}";
    String SVC_CHANGE_CREDENTIAL=SVC_USER+"/change_credential";

    @GET(SVC_SELFIE)
    List<Selfie> getSelfieList();

    @POST(SVC_SELFIE)
    Selfie addSelfie(@Body Selfie selfie);

    @GET(SVC_SELFIE_ID)
    Selfie getSelfie(@Path(ID_PARAM) long id);

    @DELETE(SVC_SELFIE_ID)
    boolean delSelfie(@Path(ID_PARAM) long id);

    @Multipart
    @POST(SVC_SELFIE_UPLOAD)
    String uploadSelfie(@Part(FILE_PARAM) TypedFile photo,@Path(ID_PARAM) long id);

    @GET(SVC_SELFIE_DOWNLOAD)
    Response downloadSelfie(@Path(ID_PARAM) long id);

    @Multipart
    @POST(SVC_AVATAR_UPLOAD)
    String uploadAvatar(@Part(FILE_PARAM) TypedFile photo);

    @GET(SVC_AVATAR_DOWNLOAD)
    Response downloadAvatar(@Path(USER_NAME_PARAM) String username);

    @FormUrlEncoded
    @POST(SVC_LOGIN)
    void login(@Field(USER_NAME_PARAM) String username,@Field(PASSWORD_PARAM) String pass,Callback<Void> callback);

    @GET(SVC_LOGOUT)
    Void logout();

    @FormUrlEncoded
    @POST(SVC_REGISTER)
    void register(@Field(USER_NAME_PARAM) String username,@Field(PASSWORD_PARAM) String pass,@Field(EMAIL_PARAM) String email,Callback<Void> callback);

    @Multipart
    @POST(SVC_SELFIE_ROTATE)
    Response rotate(@Part(FILE_PARAM) TypedFile photo,@Path(DEGREE_PARAM)int degree);

    @Multipart
    @POST(SVC_SELFIE_SEPIA)
    Response toSepia(@Part(FILE_PARAM) TypedFile photo,@Path(DEEP_PARAM)int deep);

    @Multipart
    @POST(SVC_SELFIE_BLACK_AND_WHITE)
    Response toBlackAndWhite(@Part(FILE_PARAM) TypedFile photo);

    @Multipart
    @POST(SVC_SELFIE_BRIGHTNESS)
    Response toBrightness(@Part(FILE_PARAM) TypedFile photo,@Path(BRIGHTNESS_PARAM)int bright);

    @GET(SVC_SELFIE_DOWNLOAD_PREVIEW)
    Response downloadPreviewSelfie(@Path(ID_PARAM) long id);

    @GET(SVC_AVATAR_DOWNLOAD_PREVIEW)
    Response downloadPreviewAvatar(@Path(USER_NAME_PARAM) String username);

    @GET(SVC_USER)
    User getUser();

    @FormUrlEncoded
    @POST(SVC_CHANGE_CREDENTIAL)
    User changeCredential(@Field(PASSWORD_PARAM)String password,@Field(EMAIL_PARAM)String eMail);
}
