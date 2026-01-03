package com.simats.cakeordering.api

import com.simats.cakeordering.model.*
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    // ---------- AUTH ----------
    @POST("login.php")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("baker-login.php")
    fun bakerLogin(@Body request: LoginRequest): Call<LoginResponse>

    @POST("delivery-login.php")
    fun deliveryLogin(@Body request: LoginRequest): Call<DeliveryLoginResponse>

    @POST("delivery-register.php")
    fun deliveryRegister(@Body body: Map<String, @JvmSuppressWildcards Any>): Call<BasicResponse>

    @POST("register.php")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    // ---------- BAKER REGISTER ----------
    @FormUrlEncoded
    @POST("register-baker.php")
    fun registerBaker(
        @Field("shop_name") shopName: String,
        @Field("owner_name") ownerName: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("password") password: String,
        @Field("address") address: String,
        @Field("latitude") latitude: Double,
        @Field("longitude") longitude: Double,
        @Field("specialty") specialty: String,
        @Field("years_experience") yearsExperience: Int
    ): Call<BasicResponse>

    // ---------- BAKER REGISTER WITH IMAGE ----------
    @Multipart
    @POST("register-baker.php")
    fun registerBakerWithImage(
        @Part("shop_name") shopName: okhttp3.RequestBody,
        @Part("owner_name") ownerName: okhttp3.RequestBody,
        @Part("email") email: okhttp3.RequestBody,
        @Part("phone") phone: okhttp3.RequestBody,
        @Part("password") password: okhttp3.RequestBody,
        @Part("address") address: okhttp3.RequestBody,
        @Part("latitude") latitude: okhttp3.RequestBody,
        @Part("longitude") longitude: okhttp3.RequestBody,
        @Part("specialty") specialty: okhttp3.RequestBody,
        @Part("years_experience") yearsExperience: okhttp3.RequestBody,
        @Part image: okhttp3.MultipartBody.Part
    ): Call<BasicResponse>

    // ---------- HOME ----------
    @POST("view-cakes.php")
    fun viewCakes(@Body body: Map<String, String>): Call<ViewCakesResponse>

    // ---------- BAKER ----------
    @GET("baker-dashboard.php")
    fun getBakerDashboard(
        @Query("baker_id") bakerId: Int
    ): Call<BakerDashboardResponse>

    @GET("baker-cakes.php")
    fun getBakerCakes(
        @Query("baker_id") bakerId: Int
    ): Call<BakerCakesResponse>

    // ---------- BAKER ANALYTICS ----------
    @GET("baker-analytics.php")
    fun getBakerAnalytics(
        @Query("baker_id") bakerId: Int
    ): Call<BakerAnalyticsResponse>

    // ---------- ADD CAKE ----------
    @POST("add-cake.php")
    fun addCake(
        @Body request: AddCakeRequest
    ): Call<BasicResponse>

    // ---------- CUSTOMIZATION ----------
    @GET("get-cake-customization-options.php")
    fun getCakeCustomizationOptions(
        @Query("cake_id") cakeId: Int
    ): Call<CakeCustomizationResponse>

    // ✅ ADD TO CART
    @POST("add-cart.php")
    fun addToCart(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    // ---------- CART ----------
    @GET("view-cart.php")
    fun getCart(
        @Query("user_id") userId: Int
    ): Call<CartResponse>

    @FormUrlEncoded
    @POST("view-cart.php")
    fun getCartPost(
        @Field("user_id") userId: Int
    ): Call<CartResponse>

    @POST("update-cart.php")
    fun updateCart(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    @GET("delete-cart-item.php")
    fun deleteCartItem(
        @Query("cart_id") cartId: Int
    ): Call<BasicResponse>

    // Clear cart after order placed
    @POST("clear-cart.php")
    fun clearCart(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    // Place order - saves to database
    @POST("place-order.php")
    fun placeOrder(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<PlaceOrderResponse>

    // ---------- MANAGE CAKE ----------
    @GET("get-cake-details.php")
    fun getCakeDetails(
        @Query("cake_id") cakeId: Int
    ): Call<CakeDetailsResponse>

    @GET("delete-cake.php")
    fun deleteCake(
        @Query("cake_id") cakeId: Int
    ): Call<BasicResponse>

    // ---------- EDIT CAKE ----------
    @GET("get-cake-for-edit.php")
    fun getCakeForEdit(
        @Query("cake_id") cakeId: Int
    ): Call<CakeEditResponse>

    @POST("update-cake.php")
    fun updateCake(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    // ---------- BAKER ORDERS ----------
    @GET("baker-orders.php")
    fun getBakerOrders(
        @Query("baker_id") bakerId: Int
    ): Call<BakerOrdersResponse>

    @FormUrlEncoded
    @POST("update-order-status.php")
    fun updateOrderStatus(
        @Field("order_id") orderId: Int,
        @Field("status") status: String
    ): Call<BasicResponse>

    // ---------- BAKER MESSAGES ----------
    @GET("baker-message-customers.php")
    fun getBakerMessageCustomers(
        @Query("baker_id") bakerId: Int
    ): Call<CustomerMessagesResponse>

    // ---------- CHAT ----------
    @GET("get-chat-messages.php")
    fun getChatMessages(
        @Query("baker_id") bakerId: Int,
        @Query("user_id") userId: Int
    ): Call<ChatMessagesResponse>

    @FormUrlEncoded
    @POST("send-chat-message.php")
    fun sendChatMessage(
        @Field("baker_id") bakerId: Int,
        @Field("user_id") userId: Int,
        @Field("sender_type") senderType: String,
        @Field("message") message: String?,
        @Field("image_url") imageUrl: String?
    ): Call<BasicResponse>

    // ---------- BAKER PROFILE ----------
    @GET("baker-profile.php")
    fun getBakerProfile(
        @Query("baker_id") bakerId: Int
    ): Call<BakerProfileResponse>

    // ---------- CHAT IMAGE UPLOAD ----------
    @Multipart
    @POST("upload-chat-image.php")
    fun uploadChatImage(
        @Part image: okhttp3.MultipartBody.Part
    ): Call<ImageUploadResponse>

    // ---------- PROFILE IMAGE UPLOAD ----------
    @Multipart
    @POST("upload-profile-image.php")
    fun uploadProfileImage(
        @Part("baker_id") bakerId: okhttp3.RequestBody,
        @Part image: okhttp3.MultipartBody.Part
    ): Call<ImageUploadResponse>

    // ---------- CAKE IMAGE UPLOAD ----------
    @Multipart
    @POST("upload-cake-image.php")
    fun uploadCakeImage(
        @Part image: okhttp3.MultipartBody.Part
    ): Call<ImageUploadResponse>

    // ---------- ADDRESSES ----------
    @GET("get-addresses.php")
    fun getAddresses(
        @Query("user_id") userId: Int
    ): Call<AddressResponse>

    @POST("add-address.php")
    fun addAddress(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    // ---------- CUSTOMER PROFILE ----------
    @GET("customer-profile.php")
    fun getCustomerProfile(
        @Query("user_id") userId: Int
    ): Call<CustomerProfileResponse>

    // ---------- CUSTOMER ORDERS ----------
    @GET("customer-orders.php")
    fun getCustomerOrders(
        @Query("user_id") userId: Int
    ): Call<CustomerOrdersResponse>

    // ---------- ORDER DETAILS ----------
    @GET("order-details.php")
    fun getOrderDetails(
        @Query("order_id") orderId: Int
    ): Call<OrderDetailsResponse>

    // ---------- CANCEL ORDER ----------
    @GET("cancel-order.php")
    fun cancelOrder(
        @Query("order_id") orderId: Int
    ): Call<GenericResponse>

    // ---------- UPDATE PROFILE ----------
    @FormUrlEncoded
    @POST("update-profile.php")
    fun updateProfile(
        @Field("user_id") userId: Int,
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("phone") phone: String,
        @Field("address") address: String
    ): Call<GenericResponse>

    // ---------- CUSTOMER MESSAGES ----------
    @GET("customer-messages.php")
    fun getCustomerMessages(
        @Query("user_id") userId: Int
    ): Call<CustomerMessagesResponse>

    // ---------- FAVORITES ----------
    @GET("toggle-favorite.php")
    fun toggleFavorite(
        @Query("user_id") userId: Int,
        @Query("cake_id") cakeId: Int,
        @Query("action") action: String
    ): Call<FavoriteResponse>

    @GET("get-favorites.php")
    fun getFavorites(
        @Query("user_id") userId: Int
    ): Call<FavoritesListResponse>

    // ---------- NEARBY BAKERS ----------
    @GET("get-nearby-bakers.php")
    fun getNearbyBakers(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("radius") radius: Double
    ): Call<com.simats.cakeordering.model.NearbyBakersResponse>

    // ---------- BAKER STATUS ----------
    @GET("update-baker-status.php")
    fun updateBakerStatus(
        @Query("baker_id") bakerId: Int,
        @Query("is_online") isOnline: Int
    ): Call<BasicResponse>

    // ---------- DELIVERY ----------
    @GET("delivery-dashboard.php")
    fun getDeliveryDashboard(
        @Query("delivery_id") deliveryId: Int
    ): Call<DeliveryDashboardResponse>

    @GET("delivery-orders.php")
    fun getDeliveryOrders(
        @Query("delivery_id") deliveryId: Int,
        @Query("status") status: String
    ): Call<DeliveryOrdersResponse>

    @POST("delivery-update-status.php")
    fun updateDeliveryStatus(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    @GET("delivery-profile.php")
    fun getDeliveryProfile(
        @Query("delivery_id") deliveryId: Int
    ): Call<DeliveryProfileResponse>

    @GET("delivery-toggle-online.php")
    fun toggleDeliveryOnline(
        @Query("delivery_id") deliveryId: Int,
        @Query("is_online") isOnline: Int
    ): Call<BasicResponse>

    // ---------- BAKER SET FOR DELIVERY ----------
    @POST("set-for-delivery.php")
    fun setForDelivery(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    // ---------- UPDATE DELIVERY PROFILE ----------
    @POST("update-delivery-profile.php")
    fun updateDeliveryProfile(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    // ---------- NOTIFICATIONS ----------
    @GET("get-notifications.php")
    fun getNotifications(
        @Query("user_type") userType: String,
        @Query("user_id") userId: Int
    ): Call<NotificationsResponse>

    @POST("mark-notification-read.php")
    fun markNotificationRead(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>

    // ---------- AI CAKE GENERATOR ----------
    @POST("generate_cake.php")
    fun generateAiCake(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<AiCakeResponse>

    // ---------- FCM TOKEN ----------
    @POST("save-fcm-token.php")
    fun saveFcmToken(
        @Body body: Map<String, @JvmSuppressWildcards Any>
    ): Call<BasicResponse>
}
