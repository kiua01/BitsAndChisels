# Bits and Chisels 2.7.3 -> Minecraft 26.3 Fabric Port

## Target
- Minecraft: 26.3
- Fabric Loader: 0.19.5
- Fabric API: 0.165.0+26.3
- Fabric Loom: 1.17-SNAPSHOT
- Java: 25
- Port version: 2.7.3+26.3-port.1

## Ported core features
- 16x16x16 (4096) micro-bit storage per Bits Block
- Full-block conversion to chiseled bit blocks
- Single-bit diamond chisel behavior
- 4x4x4 iron chisel behavior
- Smart chisel region behavior with server validation
- Bit item state storage using modern data components
- Bit placement across block faces
- Block entity save/load and client synchronization
- Dynamic collision/selection voxel shape
- Wrench rotate/mirror behavior
- Blueprint copy/place behavior
- 26.3 custom-payload networking
- 26.3 block/item/creative-tab registration
- 26.3 block entity render-state renderer (no raw OpenGL)
- 26.3 item-model JSON layout
- 26.3 recipe directory/result syntax
- Original textures and translations retained

## Intentionally deferred from the old build
These optional/visual compatibility layers were removed from the base port so they do not prevent 26.3 from loading:
- old REI integration
- Canvas/FREX compatibility hooks
- DashLoader integration
- Flan/GOML compatibility hooks
- old mixin-based red selection-box renderer
- old particle suppression mixin

The base renderer is correctness-first and renders visible micro-bits through the 26.3 block-model render-state API. It can be optimized with mesh batching after runtime validation.

## 26.2 -> 26.3 notes
- No usage of removed Fabric APIs (FuelRegistry, CompostingChanceRegistry, block transformer registries, etc.)
- Version bumps only + metadata updates
- Java 25 and Loom 1.17 remain correct for 26.3

## Verification state
- Resource JSON syntax: validated (from 26.2 port)
- No legacy intermediary `class_####` references remain in the port source
- No TODO/FIXME placeholders in the port source
- A Java 25 GitHub Actions workflow is included at `.github/workflows/build.yml`
- A real Loom compile has NOT been completed in this environment.

Do not publish or label a JAR as production-ready until the included Java 25 workflow passes and a client/server smoke test is performed.
