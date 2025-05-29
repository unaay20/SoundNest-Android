package com.example.soundnest_android.ui.notifications

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.R
import com.example.soundnest_android.auth.SharedPrefsTokenProvider
import com.example.soundnest_android.databinding.ActivityNotificationsBinding
import com.example.soundnest_android.network.ApiService
import com.example.soundnest_android.restful.constants.RestfulRoutes
import com.example.soundnest_android.restful.models.notification.NotificationResponse
import com.example.soundnest_android.restful.services.NotificationService

class NotificationsActivity : AppCompatActivity() {

    private val viewModel: NotificationsViewModel by viewModels {
        NotificationsViewModelFactory(
            NotificationService(
                RestfulRoutes.getBaseUrl(),
                ApiService.tokenProvider
            )
        )
    }

    private var _binding: ActivityNotificationsBinding? = null
    private val binding get() = _binding!!

    private lateinit var deleteIcon: Drawable
    private lateinit var background: ColorDrawable
    private lateinit var adapter: NotificationsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val preferences = SharedPrefsTokenProvider(this)
        val userId = preferences.id

        if (userId != -1) {
            viewModel.loadNotifications(userId)
        }

        _binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!
        background = ColorDrawable(ContextCompat.getColor(this, android.R.color.holo_red_light))

        adapter = NotificationsAdapter(mutableListOf()) { notification ->
            showNotificationDetail(notification)
        }

        binding.rvNotifications.adapter = adapter
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                showDeleteConfirmation(position)
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                if (dX > 0) {
                    background.setBounds(
                        itemView.left,
                        itemView.top,
                        itemView.left + dX.toInt(),
                        itemView.bottom
                    )
                } else if (dX < 0) {
                    background.setBounds(
                        itemView.right + dX.toInt(),
                        itemView.top,
                        itemView.right,
                        itemView.bottom
                    )
                } else {
                    background.setBounds(0, 0, 0, 0)
                }
                background.draw(c)

                val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + deleteIcon.intrinsicHeight
                val iconLeft: Int
                val iconRight: Int
                if (dX > 0) {
                    iconLeft = itemView.left + iconMargin
                    iconRight = iconLeft + deleteIcon.intrinsicWidth
                } else {
                    iconRight = itemView.right - iconMargin
                    iconLeft = iconRight - deleteIcon.intrinsicWidth
                }
                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                deleteIcon.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvNotifications)

        viewModel.notifications.observe(this) { list ->
            binding.tvNoNotifications.visibility = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE

            if (list.isNullOrEmpty()) {
                binding.tvNoNotifications.visibility = View.VISIBLE
                binding.rvNotifications.visibility = View.GONE
            } else {
                adapter.setItems(list)
            }
        }
    }

    private fun showDeleteConfirmation(position: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.lbl_delete_notification)
            .setMessage(R.string.msg_delete_notification)
            .setPositiveButton(R.string.btn_accept) { dialog, _ ->
                adapter.removeAt(position)
                viewModel.removeNotification(position)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.btn_cancel) { dialog, _ ->
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showNotificationDetail(n: NotificationResponse) {
        val frag = NotificationDetailDialogFragment.newInstance(
            title = n.title,
            body = n.notification ?: "",
            sender = n.sender ?: "Unknown",
            relevance = n.relevance,
            date = n.createdAt ?: ""
        )
        frag.show(supportFragmentManager, "NotificationDetail")
    }

    override fun onBackPressed() {
        if (binding.notificationDetailContainer.visibility == View.VISIBLE) {
            binding.notificationDetailContainer.visibility = View.GONE
            binding.rvNotifications.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        val preferences = SharedPrefsTokenProvider(this)
        val userId = preferences.id
        viewModel.loadNotifications(userId)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
