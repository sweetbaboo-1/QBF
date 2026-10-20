# QBF

QBF is a standalone Java application built with Maven. It is packaged as a `.jar` file that can be run directly from the command line.

## Prerequisites

You must have **Java 17 or higher** installed to run this application. Older versions such as Java 8 are not supported.

To verify your Java version, open a terminal or command prompt and run:

```bash
java -version
```
java version "17.0.x"
If you see a version like 1.8.0_xxx, you're running Java 8 and will need to upgrade.

# Downloading
Download a published release when available, or open this repository's **Actions** tab and select a successful **Build and test QBF** run. Download the `QBF-Windows-JAR` artifact and extract the ZIP to obtain `QBF.jar`.

# Building
To build the application, ensure you have Maven installed. Then run:

```bash
mvn clean package
```
This will compile the project and produce the runnable file `target/QBF.jar`. Build on Windows for a Windows-compatible JavaFX JAR.

# Running
To run the application after building:

```bash
java -jar target/QBF.jar
```

## Selecting Litematica regions

After you load a `.litematic`, click **Regions (selected/total)** next to
**Load Litematic** to open the region selection dialog. All regions are selected
by default, so existing workflows continue to work. Choose individual regions
or use **Select all** / **Select none** in the dialog, then click **OK**. The
main window remains compact. At least one region must be selected to export.

Coordinates entered in **Corner 1** and **Corner 2** must still describe the
bounding box of the **complete** schematic placement, even if you select only
some regions. Filtering regions does not change the coordinate origin.

If selected regions overlap, QBF exports only one waypoint per block
position. When two selected regions contain different searched block types
at the same position, the first matching region in file order supplies the
waypoint's block name/color. This also applies to Xaero and VoxelMap exports.

## Nether and End waypoints

Select **overworld**, **the_nether**, or **the_end** in the Dimension dropdown.
Enter the actual Minecraft coordinates of the placement **in that dimension**.

- **VoxelMap**: The exported line includes `dimensions:the_nether#` or
  `dimensions:the_end#`. VoxelMap stores Nether X and Z internally multiplied
  by 8 (overworld-equivalent scale). This is intentional and VoxelMap displays
  them at the correct Nether position. Y is never multiplied.
- **Xaero**: The waypoint text has dimension-local X/Y/Z and does **not**
  include the dimension. Import/save the file into Xaero's waypoint folder
  for the matching dimension (Overworld, Nether, or End).

The dimension dropdown does not convert a schematic between dimensions.
