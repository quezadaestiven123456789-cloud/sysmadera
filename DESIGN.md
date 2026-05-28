# WoodManager - Design System

## Visión General
Sistema web profesional SaaS para la gestión de talleres de carpintería. Diseño moderno, oscuro y elegante con estilo empresarial premium.

## Tema
- **Modo:** Oscuro (Dark Mode)
- **Estilo:** Moderno, minimalista, empresarial
- **Inspiración:** Dashboards SaaS premium (Material Design, Ant Design)

## Paleta de Colores

| Color | Hex | Uso |
|-------|-----|-----|
| Fondo principal | `#0a0e17` | Background body |
| Superficie | `#141b2d` | Cards, contenedores |
| Superficie elevada | `#1a2332` | Sidebar, modales |
| Borde | `#1e2a3a` | Bordes de componentes |
| Texto primario | `#ffffff` | Títulos, texto principal |
| Texto secundario | `#8b95a5` | Subtítulos, metadata |
| Madera | `#8B5E3C` | Acentos, badges |
| Dorado | `#C9A84C` | Acciones primarias, hover |
| Dorado hover | `#d4b85a` | Hover de botones primarios |
| Success | `#2ed47a` | Estados exitosos |
| Warning | `#ffb946` | Alertas, stock bajo |
| Danger | `#ff4d4f` | Errores, eliminar |

## Tipografía
- **Familia:** `'Inter', -apple-system, sans-serif`
- **Headings:** Inter SemiBold
- **Body:** Inter Regular
- **Monospace:** `'JetBrains Mono', monospace` (código, datos)

## Componentes

### Sidebar
- Fondo: `#0d1117`
- Ancho: 260px (colapsado: 70px)
- Items con icono + texto
- Hover: overlay dorado suave
- Active: borde izquierdo dorado

### Cards
- Fondo: `#141b2d`
- Border-radius: 12px
- Sombra: `0 2px 12px rgba(0,0,0,0.3)`
- Padding: 24px
- Hover: `0 4px 20px rgba(0,0,0,0.4)`

### Botones
- Border-radius: 8px
- Padding: 10px 20px
- Primario: dorado `#C9A84C`
- Outline: borde dorado
- Danger: rojo `#ff4d4f`

### Tablas
- Fondo header: `#1a2332`
- Fondo filas: `#141b2d`
- Hover fila: `#1e2a3a`
- Border-radius: 10px
- Bordes: `#1e2a3a`

### Formularios
- Input bg: `#1a2332`
- Input border: `#1e2a3a`
- Input focus: borde dorado
- Label: `#8b95a5`
- Border-radius: 8px

### Badges / Tags
- Border-radius: 6px
- Padding: 4px 12px
- Font-size: 12px
- Font-weight: 500

## Layout
- Sidebar fijo izquierdo
- Navbar superior fijo
- Contenido con scroll
- Padding contenido: 30px
- Gap grid: 24px
- Responsive breakpoints: 768px, 992px, 1200px

## Responsive
- Desktop: sidebar expandido
- Tablet: sidebar colapsable
- Mobile: sidebar oculto (toggle hamburguesa)

## Animaciones
- Transiciones: 0.3s ease
- Hover cards: translateY(-2px)
- Sidebar collapse: width 0.3s
- Fade in modales: 0.2s

## Iconografía
- Font Awesome 6 (Pro style)
- Iconos outline consistentes
- Tamaño: 18-20px en sidebar, 24px en stats
