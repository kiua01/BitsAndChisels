# Bits and Chisels 2.7.3 -> Minecraft 26.3 Fabric Port

## Target
- Minecraft: 26.3
- Fabric Loader: 0.19.5
- Fabric API: 0.165.0+26.3
- Fabric Loom: 1.17-SNAPSHOT
- Java: 25
- Port version: 2.7.3+26.3-port.2

## Complete core feature set
- 16×16×16 (4096) micro-bit storage per Bits Block
- Full-block conversion to chiseled bit blocks (left-click with chisel)
- Diamond chisel: single-bit removal
- Iron chisel: 4×4×4 region removal
- Smart chisel: two-click region selection with server validation
- Client chisel rate-limit (matches original feel)
- Tool durability (diamond/smart 1561, iron/wrench 250)
- Bit item placement across block faces
- Bit item state via modern data components
- Block entity save/load + client sync
- Dynamic collision / selection voxel shapes
- Wrench rotate/mirror
- Blueprint copy/place
- 26.3 custom-payload networking
- Creative tab with all items + bits block
- Recipes (`category: equipment` for tools)
- Textures, models, translations retained
- 26.3 block-entity render-state renderer

## Intentionally deferred (optional / visual)
- REI / JEI integration
- Canvas/FREX hooks
- DashLoader
- Flan/GOML claim hooks
- Red selection-box outline mixin (smart/diamond preview)
- Particle suppression mixin

## How to use
1. Creative tab **Bits And Chisels**, or craft:
   - Diamond + stick → diamond chisel
   - Iron + stick → iron chisel
   - Redstone block + iron chisel + diamond → smart chisel
   - Iron + iron chisel → wrench
   - Lapis + paper → blueprint
2. Left-click solid blocks with a chisel to carve bits (bits drop).
3. Place bit items on surfaces to rebuild.
4. Smart chisel: first left-click sets corner A, second sets corner B.

## Verification
- GitHub Actions Java 25 build on `agent/mc26.3-port`
- Recipe category fixed for 26.3 (`equipment`)
- No legacy intermediary class names in port sources
