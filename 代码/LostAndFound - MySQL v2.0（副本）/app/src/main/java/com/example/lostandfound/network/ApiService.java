package com.example.lostandfound.network;

import com.example.lostandfound.Comment;
import com.example.lostandfound.Conversation;
import com.example.lostandfound.LostItem;
import com.example.lostandfound.Message;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @Multipart
    @POST("api/publish")
    Call<JsonObject> publishLostItem(
            @Part("itemName") RequestBody itemName,
            @Part("category") RequestBody category,
            @Part("lostTime") RequestBody lostTime,
            @Part("lostLocation") RequestBody lostLocation,
            @Part("description") RequestBody description,
            @Part("contactInfo") RequestBody contactInfo,
            @Part("isUrgent") RequestBody isUrgent,
            @Part List<MultipartBody.Part> images
    );

    @GET("api/items/list")
    Call<List<LostItem>> getLostItems(@Query("keyword") String keyword);

    // 首页带筛选条件的失物列表接口
    @GET("api/items/listWithFilters")
    Call<List<LostItem>> getLostItemsWithFilters(
            @Query("category") String category,
            @Query("timeRange") String timeRange,
            @Query("urgentOnly") String urgentOnly
    );

    @GET("api/items/myItems")
    Call<List<LostItem>> getMyLostItems();

    @POST("api/items/updateStatus")
    Call<JsonObject> updateItemStatus(
            @Body UpdateStatusRequest request
    );

    @GET("api/comments")
    Call<List<Comment>> getComments(@Query("lost_item_id") String lostItemId);

    @POST("api/comments")
    Call<Comment> postComment(@Body Comment comment);

    // 获取失物详情的接口
    @GET("api/items/detail")
    Call<LostItem> getLostItemDetail(@Query("id") String id);

    // 删除失物的接口
    @POST("api/items/delete")
    Call<JsonObject> deleteItem(@Query("id") String id);

    // 消息相关接口
    @GET("api/messages/conversations")
    Call<ApiResponse<List<Map<String, Object>>>> getConversations(@Query("userId") String userId);

    @GET("api/messages")
    Call<ApiResponse<List<Message>>> getMessagesByConversation(@Query("conversationId") String conversationId);

    @POST("api/messages")
    Call<ApiResponse<Message>> sendMessage(@Body Message message);

    // 标记当前会话消息为已读
    @POST("api/messages/markRead")
    Call<ApiResponse<Void>> markMessagesAsRead(@Body JsonObject requestBody);

    // 标记所有消息为已读
    @POST("api/messages/markAllRead")
    Call<ApiResponse<Void>> markAllMessagesAsRead(@Body JsonObject body);

    @GET("api/user")
    Call<ApiResponse<JsonObject>> getUserProfile(@Query("userId") String userId);

    @POST("api/user")
    Call<ApiResponse<Void>> logout(@Body JsonObject body);

    @Multipart
    @POST("api/user/update")
    Call<ApiResponse<JsonObject>> updateProfile(
            @Part("userId") RequestBody userId,
            @Part("username") RequestBody username,
            @Part("email") RequestBody email,
            @Part("phone") RequestBody phone,
            @Part MultipartBody.Part avatar
    );

    @GET("api/captcha")
    Call<JsonObject> getCaptcha();

    @FormUrlEncoded
    @POST("api/user/resetPassword")
    Call<JsonObject> resetPassword(@FieldMap Map<String, String> params);

}