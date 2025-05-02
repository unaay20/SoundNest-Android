package com.example.soundnest_android.ui.notifications

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.soundnest_android.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private var _binding: ActivityNotificationsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: NotificationsViewModel by viewModels()

    private lateinit var deleteIcon: Drawable
    private lateinit var background: ColorDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deleteIcon = ContextCompat.getDrawable(this, android.R.drawable.ic_menu_delete)!!
        background = ColorDrawable(ContextCompat.getColor(this, android.R.color.holo_red_light))

        val adapter = NotificationsAdapter(mutableListOf()) { notification ->
            Toast.makeText(this, "Notificación seleccionada: $notification", Toast.LENGTH_SHORT).show()
        }

        binding.rvNotifications.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.layoutManager = layoutManager

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                if (direction == ItemTouchHelper.LEFT) {
                    showDeleteConfirmation(position)
                }
            }

            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                if (dX > 0) {
                    background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    background.draw(c)

                    val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - deleteIcon.intrinsicHeight) / 2
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + deleteIcon.intrinsicWidth
                    val iconBottom = iconTop + deleteIcon.intrinsicHeight

                    deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon.draw(c)
                } else {
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvNotifications)

        viewModel.notifications.observe(this) { notifications ->
            adapter.addSearch("Nueva notificación")
            adapter.addSearch("👋 ¡Bienvenido! Estas son tus notificaciones.")
            adapter.addSearch("🔔 Tienes 3 solicitudes de amistad pendientes.")
            adapter.addSearch("📸 Alguien comentó tu foto.")
        }
    }

    private fun showDeleteConfirmation(position: Int) {
        Toast.makeText(this, "¿Eliminar esta notificación?", Toast.LENGTH_SHORT).show()

        viewModel.removeNotification(position)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
