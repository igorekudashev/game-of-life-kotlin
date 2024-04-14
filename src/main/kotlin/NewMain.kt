import com.aparapi.Kernel
import com.aparapi.Range
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.awt.image.DataBufferInt
import java.lang.Boolean
import javax.swing.*
import kotlin.Array
import kotlin.Int
import kotlin.IntArray
import kotlin.Long
import kotlin.String

object NewMain {
    var running = false

    @JvmStatic
    fun main(_args: Array<String>) {
        val frame = JFrame("Game of Life")
        val width = Integer.getInteger("width", 1024 + 512 + 256 + 128)
        val height = Integer.getInteger("height", 768 + 256)

        // Buffer is twice the size as the screen.  We will alternate between mutating data from top to bottom
        // and bottom to top in alternate generation passses. The LifeKernel will track which pass is which
        val image = BufferedImage(width, height * 2, BufferedImage.TYPE_INT_RGB)
        val lifeKernel = LifeKernel(width, height, image)

        // Create a component for viewing the offsecreen image
        val viewer: JComponent = object : JComponent() {
            public override fun paintComponent(g: Graphics) {
                if (lifeKernel.isExplicit) {
                    lifeKernel[lifeKernel.imageData] // We only pull the imageData when we intend to use it.
                    val profileInfo = lifeKernel.profileInfo
                    if (profileInfo != null) {
                        for (p in profileInfo) {
                            print(" " + p.label + " " + p.start / 1000 + " .. " + p.end / 1000 + " " + (p.end - p.start) / 1000 + "us")
                        }
                    }
                }
                // We copy one half of the offscreen buffer to the viewer, we copy the half that we just mutated.
                if (lifeKernel.fromBase == 0) {
                    g.drawImage(image, 0, 0, width, height, 0, 0, width, height, this)
                } else {
                    g.drawImage(image, 0, 0, width, height, 0, height, width, 2 * height, this)
                }
            }
        }
        val controlPanel = JPanel(FlowLayout())
        frame.contentPane.add(controlPanel, BorderLayout.SOUTH)
        val startButton = JButton("Start")
        startButton.addActionListener {
            running = true
            startButton.isEnabled = false
        }
        controlPanel.add(startButton)
        controlPanel.add(JLabel(lifeKernel.targetDevice.shortDescription))
        controlPanel.add(JLabel("  Generations/Second="))
        val generationsPerSecond = JLabel("0.00")
        controlPanel.add(generationsPerSecond)

        // Set the default size and add to the frames content pane
        viewer.preferredSize = Dimension(width, height)
        frame.contentPane.add(viewer)

        // Swing housekeeping
        frame.pack()
        frame.isVisible = true
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        var start = System.currentTimeMillis()
        var generations: Long = 0
        while (!running) {
            try {
                Thread.sleep(10)
                viewer.repaint()
            } catch (e1: InterruptedException) {
                // TODO Auto-generated catch block
                e1.printStackTrace()
            }
        }
        while (true) {
            lifeKernel.nextGeneration() // Work is performed here
            viewer.repaint() // Request a repaint of the viewer (causes paintComponent(Graphics) to be called later not synchronous
            generations++
            val now = System.currentTimeMillis()
            if (now - start > 1000) {
                generationsPerSecond.text = String.format("%5.2f", generations * 1000.0 / (now - start))
                start = now
                generations = 0
            }
        }
    }

    class LifeKernel(_width: Int, _height: Int, _image: BufferedImage) : Kernel() {
        val imageData: IntArray
        private val width: Int
        private val height: Int
        private var range: Range? = null
        var fromBase: Int
        private var toBase: Int
        private fun processPixel(gid: Int) {
            val to = gid + toBase
            val from = gid + fromBase
            val x = gid % width
            val y = gid / width
            if (x == 0 || x == width - 1 || y == 0 || y == height - 1) {
                // This pixel is on the border of the view, just keep existing value
                imageData[to] = imageData[from]
            } else {
                // Count the number of neighbors.  We use (value&1x) to turn pixel value into either 0 or 1
                val neighbors = (imageData[from - 1] and 1) +  // EAST
                        (imageData[from + 1] and 1) +  // WEST
                        (imageData[from - width - 1] and 1) +  // NORTHEAST
                        (imageData[from - width] and 1) +  // NORTH
                        (imageData[from - width + 1] and 1) +  // NORTHWEST
                        (imageData[from + width - 1] and 1) +  // SOUTHEAST
                        (imageData[from + width] and 1) +  // SOUTH
                        (imageData[from + width + 1] and 1) // SOUTHWEST

                // The game of life logic
                if (neighbors == 3 || neighbors == 2 && imageData[from] == ALIVE) {
                    imageData[to] = ALIVE
                } else {
                    imageData[to] = DEAD
                }
            }
        }

        override fun run() {
            val gid = globalId
            processPixel(gid)
        }

        var sequential = Boolean.getBoolean("sequential")

        init {
            imageData = (_image.raster.dataBuffer as DataBufferInt).data
            width = _width
            height = _height
            range = Range.create(width * height, 4)
//            val executionMode = System.getProperty("com.aparapi.executionMode")
//            range = if (executionMode != null && executionMode == "JTP") {
//                Range.create(width * height, 4)
//            } else {
//                Range.create(width * height)
//            }
            println("range = $range")
            fromBase = height * width
            toBase = 0
            isExplicit = true // This gives us a performance boost
            /** draw a line across the image  */
            for (i in width * (height / 2) + width / 10 until width * (height / 2 + 1) - width / 10) {
                imageData[i] = ALIVE
            }
            put(imageData) // Because we are using explicit buffer management we must put the imageData array
        }

        fun nextGeneration() {
            // swap fromBase and toBase
            val swap = fromBase
            fromBase = toBase
            toBase = swap
            if (sequential) {
                for (gid in 0 until width * height) {
                    processPixel(gid)
                }
            } else {
                execute(range)
            }
        }

        companion object {
            private const val ALIVE = 0xffffff
            private const val DEAD = 0
        }
    }
}