# Animation Format
This guide aims to show you how to use Minecraft's default animation format to animate Minecraft textures with MoreMcmeta.

## Table of Contents
* [Vanilla's Format vs. MoreMcmeta's Format](#vanillas-format-vs-moremcmetas-format)
    * [Vanilla-Animated vs. MoreMcmeta-Animated Textures List](#vanilla-animated-vs-moremcmeta-animated-textures-list)
* [Animation Properties](#animation-properties)
    * [Structure](#structure)
    * [Property Definitions](#property-definitions)
    * [Rectangular Frames](#rectangular-frames)
    * [File Names](#file-names)
    * [`.moremcmeta` Examples](#moremcmeta-examples)
* [Image Layout](#image-layout)
    * [Image Example](#image-example)

## Vanilla's Format vs. MoreMcmeta's Format
Presently, the only change MoreMcmeta makes to the default animation format is using `.moremcmeta` as the file extension instead of `.mcmeta`.

MoreMcmeta also supports additional, non-animation parameters: `blur` and `clamp`. Vanilla supports these extra parameters for non-animated textures only.

### Vanilla-Animated vs. MoreMcmeta-Animated Textures List
Use the **vanilla format** for these types of textures:
* blocks and tile entities
* items
* paintings
* particles
* status effect icons ("mob effects")

Use the **MoreMcmeta format** for these types of textures:
* mobs
* other entities (such as the fishing rod's bobber)
* armor
* environmental textures (such as the sun and moon)
* GUIs (including main menu textures besides the Mojang logo)
* map backgrounds
* "miscellaneous" textures (such as the enchantment glint and the shadow)

You cannot animate colormaps because they are only used when a chunk reloads.

## Animation Properties
### Structure
See [Examples](#moremcmeta-examples) for examples of correct syntax. This chart merely describes how properties are nested.
```
.
+-- "texture"
|   +-- "blur"
|   +-- "clamp"
+-- "animation"
|   +-- "interpolate"
|   +-- "width"
|   +-- "height"
|   +-- "frametime"
|   +-- "frames"
    |   +-- frame index
    |   +-- additional frame properties
        |   +-- "index"
        |   +-- "time"
``` 

### Property Definitions
All of these properties are optional. The default value will be used if you do not specify them.

| Property | Description | Type | Default |
| ---      | ---         | ---  | ---
| `"blur"` | blurs the texture | boolean | `false`
| `"clamp"` | prevents the texture from repeating. Only use `true` when undesirable repeating is visible. | boolean | `false`
| `"interpolate"` | generates transitional frames between frames that are longer than one tick | boolean | `false`
| `"width"` | frame width (pixels) | integer | guessed from image dimensions
| `"height"` | frame height (pixels) | integer | guessed from image dimensions
| `"frametime"` | frame length (ticks or 1/20th of a second) | integer | `1`
| `"frames"` | time and order of individual frames | array | determined from `frametime` and order of frames in image
| `"index"` | the index of an individual frame (0 for first frame) | integer | determined from order of frames in image
| `"time"` | length of a specific frame | integer | value of `frametime`

### Rectangular Frames
It is recommended that you explicitly define a frame width and height for rectangular frames. When you do not provide a width and a height, the mod tries to guess what those values are. It is more difficult to guess the dimensions of rectangular frames, which may cause MoreMcmeta to not animate some textures.

### File Names
The file name of the `.moremcmeta` file must match the original texture's name **exactly** with the `.moremcmeta` suffix appended.

For example:
* `bat.png` > `bat.png.moremcmeta`
* `moon_phases.png` > `moon_phases.png.moremcmeta`
* `inventory.png` > `inventory.png.moremcmeta`

Note that `.moremcmeta` is the file extension; a file called `inventory.png.moremcmeta.txt` is incorrect. You may need to enable file extension visibility in your file explorer to change the extension.

### `.moremcmeta` Examples
#### Bare Minimum
```
{
  "animation": {}
}
```

#### All Properties
```
{
    "texture": {
        "blur": true,
        "clamp": true
    },
    "animation": {
        "interpolate": true,
        "width": 64,
        "height": 64,
        "frametime": 10,
        "frames": [
            {
                "index": 0,
                "time": 5
            },
            1, 3, 2, 
            {
                "index": 4,
                "time": 2
            },
            8, 3
        ]
    }
}
```

#### Frame Time and Interpolation
```
{
    "animation": {
        "interpolate": true,
        "frametime": 10
    }
}
```

#### Rectangular Frames
```
{
    "animation": {
        "width": 128,
        "height": 64
    }
}
```

## Image Layout
Animated textures should be `.png` files with the exact same name as their `.moremcmeta` files, minus the `.moremcmeta` suffix.
* `bat.png.moremcmeta` > `bat.png`
* `moon_phases.png.moremcmeta` > `moon_phases.png`
* `inventory.png.moremcmeta` > `inventory.png`

All frames must have identical dimensions and be in the same image. You cannot separate frames into individual files.

The numbers in this section represent the indices of frames in an image. A frame's index is how many frames away it is from the first frame in the image. Therefore, the first frame's index is 0, the second frame's index is 1, and so on.

Typically, all frames are placed in a column with the first frame on top:
```
0
1
2
3
4
...
```

You can also place the frames in a row:
```
0 1 2 3 4 ...
```

Placing frames in a rectangular format is possible but not recommended:
```
0 1 2
3 4 5
...
```

You should specify frame width and height in the `.moremcmeta` file if you do not place frames in a single row or column. This format only works well when all of your frames fit the rectangle perfectly. Otherwise, you have to explicitly skip the indices of blank frames at the end in the `.moremcmeta` file.

### Image Example
An animated inventory texture with the column format:

![animated inventory texture with the column format](https://github.com/soir20/MoreMcmeta/blob/main/info/img/inventory-column-example.png?raw=true)