package base

import java.util.UUID
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicReference

private val OPERATION_PERMISSION: ThreadLocal<UUID> = ThreadLocal()

fun setThreadPermission(permission: UUID? = null) {
    if (permission != null) {
        OPERATION_PERMISSION.set(permission)
    } else {
        OPERATION_PERMISSION.remove()
    }
}

class PermissionLock {

    private val currentThreadPermission: AtomicReference<UUID> = AtomicReference(null)
    private val semaphore: Semaphore = Semaphore(1, true)

    fun run(action: () -> Unit) {

        when (OPERATION_PERMISSION.get()) {
            currentThreadPermission.get() -> {
                action.invoke()
            }
            null -> {
                println("Method run() allowed only for prepared operation with permission")
                throw IllegalThreadStateException("Method run() allowed only for prepared operation with permission")
            }
            else -> {
                println("Operation with permission should be prepared first")
                throw IllegalThreadStateException("Operation with permission should be prepared first")
            }
        }
    }

    fun prepareLock(action: (UUID) -> Unit) {
        val permission = UUID.randomUUID()
        semaphore.acquire()
        currentThreadPermission.set(permission)
        action.invoke(permission)
        currentThreadPermission.set(null)
        semaphore.release()
    }
}