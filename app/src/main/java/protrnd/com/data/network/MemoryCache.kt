package protrnd.com.data.network

import protrnd.com.data.models.*

object MemoryCache {
    var transactionsMap = mutableListOf<Transaction>()
    val profiles = mutableMapOf<String, Profile>()
    val posts = mutableListOf<Post>()
    var chats = mutableMapOf<String, MutableList<Chat>>()
    val postLikes = mutableMapOf<String, Int>()
    val likedPosts = mutableSetOf<String>()
    var conversations = mutableListOf<Conversation>()
    val balance = mutableMapOf<String, Double>()
    var transactionsList = mutableListOf<Transaction>()
    var allNotifications = mutableListOf<Notification>()
    var commentsMap = mutableMapOf<String, List<Comment>>()
    var profilePosts = mutableMapOf<String, MutableList<Post>>()
    val profileFollowers = mutableMapOf<String, String>()
    val profileFollowings = mutableMapOf<String, String>()
    val hashTagPosts = mutableMapOf<String, List<Post>>()
    val hashTagPostCount = mutableMapOf<String, Int>()
}