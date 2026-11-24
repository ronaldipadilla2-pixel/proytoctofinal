package com.example.proyectofinal

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

data class Plato(
    val id: Int,
    val nombre: String,
    val precio: Double,
    val precioTexto: String,
    val contenido: String,
    val imagenRes: Int
)

data class ItemCarrito(
    val plato: Plato,
    val cantidad: Int,
    val nota: String
)

enum class PantallaApp {
    SELECTOR_LOGIN,
    LOGIN_CLIENTE,
    LOGIN_REPARTIDOR,
    UBICACION_CLIENTE,
    MENU_CLIENTE,
    CARRITO_CLIENTE,
    PAGO_CLIENTE,
    SEGUIMIENTO_CLIENTE,
    PANEL_REPARTIDOR
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Configuration.getInstance().userAgentValue = "$packageName-TuChasquiGo"
        setContent {
            MaterialTheme {
                TuChasquiGoApp()
            }
        }
    }
}

@Composable
fun TuChasquiGoApp() {
    val catalogo = remember {
        listOf(
            Plato(1, "Pollo Broaster", 12.00, "S/ 10.00 - S/ 14.00",
                "Pieza de pollo broaster crocante (ala o pierna), porción de papas fritas, ensalada de col con zanahoria y cremas.",
                R.drawable.imagen1),

            Plato(2, "Salchipapa Clásica", 7.00, "S/ 6.00 - S/ 8.00",
                "Porción de papas fritas doradas, salchicha tipo hot dog en rodajas y acompañamiento de cremas (mayonesa, kétchup y mostaza).",
                R.drawable.imagen2),

            Plato(3, "Salchipapa Mediana", 9.00, "S/ 8.00 - S/ 10.00",
                "Porción mediana de papas fritas, salchichas tipo hot dog en rodajas y variedad de cremas.",
                R.drawable.imagen3),

            Plato(4, "Salchipapa Grande", 12.00, "S/ 10.00 - S/ 14.00",
                "Porción abundante de papas fritas, generosa cantidad de salchichas en rodajas y acompañamiento de mayonesa, kétchup y mostaza.",
                R.drawable.imagen4),

            Plato(5, "Sánguche de Pollo", 6.00, "S/ 5.00 - S/ 8.00",
                "Pan francés relleno de pollo deshilachado, mayonesa, lechuga fresca y cremas al gusto.",
                R.drawable.imagen5),

            Plato(6, "Sánguche de Hamburguesa", 7.00, "S/ 6.00 - S/ 9.00",
                "Pan de hamburguesa con carne de hamburguesa, lechuga fresca, rodajas de tomate, papas al hilo y cremas.",
                R.drawable.imagen6),

            Plato(7, "Caldo de Pollo", 8.00, "S/ 7.00 - S/ 10.00",
                "Caldo preparado a base de pollo, presa de pollo, fideos, papa, cebolla china y hierbas aromáticas.",
                R.drawable.imagen7),

            Plato(8, "Coca-Cola 500 ml", 3.00, "S/ 2.50 - S/ 3.50",
                "Bebida gaseosa Coca-Cola personal de 500 ml, ideal para acompañar tus comidas.",
                R.drawable.imagen8),

            Plato(9, "Inca Kola 500 ml", 3.00, "S/ 2.50 - S/ 3.50",
                "Bebida gaseosa Inca Kola personal de 500 ml, refrescante y perfecta para acompañar tus platos.",
                R.drawable.imagen9),

            Plato(10, "Gaseosa KR 450 ml", 2.00, "S/ 1.50 - S/ 2.50",
                "Bebida gaseosa KR personal de 450 ml, presentación individual.",
                R.drawable.imagen10),
            )
    }

    val etapasPedido = listOf(
        "1. Recepción de cocina",
        "2. Preparación de pedido",
        "3. Despacho de pedido",
        "4. Pedido en camino",
        "5. Pedido entregado"
    )

    var pantallaActual by remember { mutableStateOf(PantallaApp.SELECTOR_LOGIN) }

    var correoCliente by remember { mutableStateOf("cliente@chasquigo.pe") }
    var correoRepartidor by remember { mutableStateOf("repartidor@chasquigo.pe") }
    var nombreCliente by remember { mutableStateOf("Ronaldi Padilla") }
    var nombreRepartidor by remember { mutableStateOf("Chasqui Motorizado 01") }
    var telefonoCliente by remember { mutableStateOf("952123456") }
    var telefonoRepartidor by remember { mutableStateOf("952654321") }

    var direccionTexto by remember { mutableStateOf("Tarata - Plaza Principal") }
    var latitud by remember { mutableDoubleStateOf(-17.5211) }
    var longitud by remember { mutableDoubleStateOf(-70.0272) }
    var costoEnvio by remember { mutableDoubleStateOf(3.00) }

    var carrito by remember { mutableStateOf(listOf<ItemCarrito>()) }
    var pedidoActivo by remember { mutableStateOf(false) }
    var pedidoTomadoPorRepartidor by remember { mutableStateOf(false) }
    var indiceEtapa by remember { mutableIntStateOf(0) }
    var metodoPago by remember { mutableStateOf("Yape") }
    var boletaTexto by remember { mutableStateOf("") }
    var conformidadCliente by remember { mutableStateOf(false) }

    val subtotal = carrito.sumOf { it.plato.precio * it.cantidad }
    val totalPagar = subtotal + costoEnvio

    Surface(modifier = Modifier.fillMaxSize(), color = Color(0xFFF4F5F7)) {
        when (pantallaActual) {
            PantallaApp.SELECTOR_LOGIN -> {
                PantallaSelectorLogin(
                    onIrLoginCliente = { pantallaActual = PantallaApp.LOGIN_CLIENTE },
                    onIrLoginRepartidor = { pantallaActual = PantallaApp.LOGIN_REPARTIDOR }
                )
            }

            PantallaApp.LOGIN_CLIENTE -> {
                PantallaLoginRol(
                    titulo = "Login Cliente",
                    subtitulo = "Ingresa para pedir en Tu Chasqui Go",
                    correo = correoCliente,
                    onCorreoChange = { correoCliente = it },
                    nombre = nombreCliente,
                    onNombreChange = { nombreCliente = it },
                    telefono = telefonoCliente,
                    onTelefonoChange = { telefonoCliente = it },
                    colorBoton = Color(0xFF8B1E24),
                    onVolver = { pantallaActual = PantallaApp.SELECTOR_LOGIN },
                    onIngresar = { pantallaActual = PantallaApp.UBICACION_CLIENTE }
                )
            }

            PantallaApp.LOGIN_REPARTIDOR -> {
                PantallaLoginRol(
                    titulo = "Login Repartidor",
                    subtitulo = "Ingresa para recepcionar y entregar pedidos",
                    correo = correoRepartidor,
                    onCorreoChange = { correoRepartidor = it },
                    nombre = nombreRepartidor,
                    onNombreChange = { nombreRepartidor = it },
                    telefono = telefonoRepartidor,
                    onTelefonoChange = { telefonoRepartidor = it },
                    colorBoton = Color(0xFF155E75),
                    onVolver = { pantallaActual = PantallaApp.SELECTOR_LOGIN },
                    onIngresar = { pantallaActual = PantallaApp.PANEL_REPARTIDOR }
                )
            }

            PantallaApp.UBICACION_CLIENTE -> {
                PantallaUbicacionCliente(
                    nombreCliente = nombreCliente,
                    direccion = direccionTexto,
                    onDireccionChange = { direccionTexto = it },
                    lat = latitud,
                    lng = longitud,
                    costoEnvio = costoEnvio,
                    onCerrarSesion = { pantallaActual = PantallaApp.SELECTOR_LOGIN },
                    onUbicacionFijada = { nuevaLat, nuevaLng, nuevaDir ->
                        latitud = nuevaLat
                        longitud = nuevaLng
                        direccionTexto = nuevaDir
                        costoEnvio = 3.00
                    },
                    onContinuarMenu = { pantallaActual = PantallaApp.MENU_CLIENTE }
                )
            }

            PantallaApp.MENU_CLIENTE -> {
                PantallaMenuCliente(
                    catalogo = catalogo,
                    direccion = direccionTexto,
                    cantidadCarrito = carrito.sumOf { it.cantidad },
                    subtotal = subtotal,
                    onCambiarUbicacion = { pantallaActual = PantallaApp.UBICACION_CLIENTE },
                    onAgregarPlato = { plato, nota ->
                        carrito = carrito + ItemCarrito(plato, 1, nota)
                    },
                    onIrCarrito = { pantallaActual = PantallaApp.CARRITO_CLIENTE }
                )
            }

            PantallaApp.CARRITO_CLIENTE -> {
                PantallaCarritoCliente(
                    carrito = carrito,
                    subtotal = subtotal,
                    costoEnvio = costoEnvio,
                    total = totalPagar,
                    direccion = direccionTexto,
                    onSeguirComprando = { pantallaActual = PantallaApp.MENU_CLIENTE },
                    onIrPago = { pantallaActual = PantallaApp.PAGO_CLIENTE }
                )
            }

            PantallaApp.PAGO_CLIENTE -> {
                PantallaPagoCliente(
                    total = totalPagar,
                    metodoPago = metodoPago,
                    onSeleccionarMetodo = { metodoPago = it },
                    onVolverCarrito = { pantallaActual = PantallaApp.CARRITO_CLIENTE },
                    onConfirmarPago = {
                        val totalFormateado = String.format(java.util.Locale.US, "%.2f", totalPagar)
                        boletaTexto = "BOL-001 | $nombreCliente ($telefonoCliente) | $metodoPago | S/ $totalFormateado"
                        pedidoActivo = true
                        pedidoTomadoPorRepartidor = false
                        indiceEtapa = 0
                        conformidadCliente = false
                        pantallaActual = PantallaApp.SEGUIMIENTO_CLIENTE
                    }
                )
            }

            PantallaApp.SEGUIMIENTO_CLIENTE -> {
                PantallaSeguimientoCliente(
                    boleta = boletaTexto,
                    direccion = direccionTexto,
                    lat = latitud,
                    lng = longitud,
                    etapas = etapasPedido,
                    indiceEtapa = indiceEtapa,
                    conformidad = conformidadCliente,
                    onConfirmarRecepcion = { conformidadCliente = true },
                    onIrVistaRepartidor = { pantallaActual = PantallaApp.LOGIN_REPARTIDOR },
                    onNuevoPedido = {
                        carrito = emptyList()
                        pedidoActivo = false
                        pantallaActual = PantallaApp.MENU_CLIENTE
                    }
                )
            }

            PantallaApp.PANEL_REPARTIDOR -> {
                PantallaPanelRepartidor(
                    nombreRepartidor = "$nombreRepartidor • $telefonoRepartidor",
                    pedidoActivo = pedidoActivo,
                    cliente = "$nombreCliente ($telefonoCliente)",
                    direccion = direccionTexto,
                    lat = latitud,
                    lng = longitud,
                    total = totalPagar,
                    pedidoTomado = pedidoTomadoPorRepartidor,
                    indiceEtapa = indiceEtapa,
                    etapas = etapasPedido,
                    onCerrarSesion = { pantallaActual = PantallaApp.SELECTOR_LOGIN },
                    onTomarPedido = {
                        pedidoTomadoPorRepartidor = true
                        if (indiceEtapa < 2) indiceEtapa = 2
                    },
                    onAvanzarEtapa = {
                        if (indiceEtapa < etapasPedido.lastIndex) {
                            indiceEtapa++
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun PantallaSelectorLogin(
    onIrLoginCliente: () -> Unit,
    onIrLoginRepartidor: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tu Chasqui Go",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B1E24)
        )
        Text(
            text = "Selecciona el tipo de login",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Login Cliente", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Ubica tu entrega con GPS, arma tu pedido, paga con Yape o Plin y sigue tu orden.", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = onIrLoginCliente,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E24))
                ) {
                    Text("Ingresar como Cliente")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Login Repartidor", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text("Recepciona pedidos pagados, revisa la ubicación del cliente y actualiza cada etapa.", style = MaterialTheme.typography.bodySmall)
                Button(
                    onClick = onIrLoginRepartidor,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF155E75))
                ) {
                    Text("Ingresar como Repartidor")
                }
            }
        }
    }
}

@Composable
fun PantallaLoginRol(
    titulo: String,
    subtitulo: String,
    correo: String,
    onCorreoChange: (String) -> Unit,
    nombre: String,
    onNombreChange: (String) -> Unit,
    telefono: String,
    onTelefonoChange: (String) -> Unit,
    colorBoton: Color,
    onVolver: () -> Unit,
    onIngresar: () -> Unit
) {
    var clave by remember { mutableStateOf("123456") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = onVolver) {
            Text("← Cambiar tipo de login")
        }

        Text(titulo, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colorBoton)
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = nombre,
            onValueChange = onNombreChange,
            label = { Text("Nombre") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = telefono,
            onValueChange = onTelefonoChange,
            label = { Text("Número de celular") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = correo,
            onValueChange = onCorreoChange,
            label = { Text("Correo") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onIngresar,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorBoton)
        ) {
            Text("Iniciar Sesión")
        }
    }
}

@Composable
fun PantallaUbicacionCliente(
    nombreCliente: String,
    direccion: String,
    onDireccionChange: (String) -> Unit,
    lat: Double,
    lng: Double,
    costoEnvio: Double,
    onCerrarSesion: () -> Unit,
    onUbicacionFijada: (Double, Double, String) -> Unit,
    onContinuarMenu: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val fusedClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    var estado by remember { mutableStateOf("Presiona el botón para obtener tu ubicación") }
    var mapView by remember { mutableStateOf<MapView?>(null) }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView?.onPause()
        }
    }

    fun obtenerUbicacion() {
        if (checkSelfPermission(context, ACCESS_FINE_LOCATION) != PERMISSION_GRANTED) return
        estado = "Obteniendo ubicación..."
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                if (loc == null) {
                    estado = "GPS inactivo. Inténtalo de nuevo."
                    return@addOnSuccessListener
                }
                val punto = GeoPoint(loc.latitude, loc.longitude)
                estado = "Lat: ${"%.5f".format(loc.latitude)}, Lng: ${"%.5f".format(loc.longitude)}"
                onUbicacionFijada(
                    loc.latitude,
                    loc.longitude,
                    "GPS (${"%.4f".format(loc.latitude)}, ${"%.4f".format(loc.longitude)})"
                )
                mapView?.apply {
                    controller.animateTo(punto)
                    controller.setZoom(17.0)
                    overlays.clear()
                    overlays.add(Marker(this).apply {
                        position = punto
                        title = "¡Estás aquí!"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        showInfoWindow()
                    })
                    invalidate()
                }
            }
    }

    val launcher = rememberLauncherForActivityResult(RequestPermission()) { granted ->
        if (granted) obtenerUbicacion() else estado = "Permiso denegado."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hola, $nombreCliente", fontWeight = FontWeight.Bold, color = Color(0xFF8B1E24))
            TextButton(onClick = onCerrarSesion) { Text("Salir") }
        }

        Box(modifier = Modifier.fillMaxWidth().height(250.dp).clip(RoundedCornerShape(14.dp))) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    MapView(ctx).apply {
                        setTileSource(TileSourceFactory.MAPNIK)
                        setMultiTouchControls(true)
                        val inicio = GeoPoint(lat, lng)
                        controller.setZoom(15.0)
                        controller.setCenter(inicio)
                        overlays.add(Marker(this).apply {
                            position = inicio
                            title = "¡Estás aquí!"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            showInfoWindow()
                        })
                        mapView = this
                        onResume()
                    }
                }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(estado, modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED) {
                        obtenerUbicacion()
                    } else {
                        launcher.launch(ACCESS_FINE_LOCATION)
                    }
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(10.dp)
            ) {
                Text("Obtener mi Ubicación")
            }
        }

        OutlinedTextField(
            value = direccion,
            onValueChange = onDireccionChange,
            label = { Text("Dirección o referencia de entrega") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Text("Envío estimado: S/ ${"%.2f".format(costoEnvio)}", fontWeight = FontWeight.Bold)

        Button(
            onClick = onContinuarMenu,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E24))
        ) {
            Text("Confirmar Ubicación y Ver Menú")
        }
    }
}

@Composable
fun PantallaMenuCliente(
    catalogo: List<Plato>,
    direccion: String,
    cantidadCarrito: Int,
    subtotal: Double,
    onCambiarUbicacion: () -> Unit,
    onAgregarPlato: (Plato, String) -> Unit,
    onIrCarrito: () -> Unit
) {
    var notas by remember { mutableStateOf(mapOf<Int, String>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Carta • Tu Chasqui Go",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF8B1E24)
                    )
                    Text(
                        text = "Entrega: $direccion",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray
                    )
                }
                FilledTonalButton(
                    onClick = onCambiarUbicacion,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Mapa")
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            itemsIndexed(catalogo) { index, plato ->
                val notaActual = notas[plato.id] ?: ""
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(165.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(id = plato.imagenRes),
                                contentDescription = plato.nombre,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Surface(
                                color = Color(0xCC111827),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                color = Color(0xFF8B1E24),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "S/ ${"%.2f".format(plato.precio)}",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }

                        Text(
                            text = plato.nombre,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Rango referencial: ${plato.precioTexto}",
                            color = Color(0xFF8B1E24),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = plato.contenido,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.DarkGray
                        )

                        OutlinedTextField(
                            value = notaActual,
                            onValueChange = { notas = notas + (plato.id to it) },
                            label = { Text("Nota especial (opcional)") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Button(
                            onClick = {
                                onAgregarPlato(plato, notaActual)
                                notas = notas + (plato.id to "")
                            },
                            modifier = Modifier.fillMaxWidth().height(44.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8B1E24))
                        ) {
                            Text("Agregar al Pedido", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Surface(
            color = Color.White,
            shadowElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$cantidadCarrito ítem(s) en carrito",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Subtotal: S/ ${"%.2f".format(subtotal)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onIrCarrito,
                    enabled = cantidadCarrito > 0,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))
                ) {
                    Text("Ver Carrito", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PantallaCarritoCliente(
    carrito: List<ItemCarrito>,
    subtotal: Double,
    costoEnvio: Double,
    total: Double,
    direccion: String,
    onSeguirComprando: () -> Unit,
    onIrPago: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Verificación del Pedido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Entrega: $direccion", style = MaterialTheme.typography.bodySmall)

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(carrito) { item ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${item.cantidad}x ${item.plato.nombre} - S/ ${"%.2f".format(item.plato.precio * item.cantidad)}", fontWeight = FontWeight.Bold)
                        if (item.nota.isNotBlank()) {
                            Text("Nota: ${item.nota}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Subtotal: S/ ${"%.2f".format(subtotal)}")
                Text("Envío: S/ ${"%.2f".format(costoEnvio)}")
                Text("Total: S/ ${"%.2f".format(total)}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onSeguirComprando, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
                Text("Menú")
            }
            Button(
                onClick = onIrPago,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))
            ) {
                Text("Pagar")
            }
        }
    }
}

@Composable
fun PantallaPagoCliente(
    total: Double,
    metodoPago: String,
    onSeleccionarMetodo: (String) -> Unit,
    onVolverCarrito: () -> Unit,
    onConfirmarPago: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        TextButton(onClick = onVolverCarrito) { Text("← Volver al carrito") }
        Text("Pago con Billetera Digital", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Total a pagar: S/ ${"%.2f".format(total)}", style = MaterialTheme.typography.titleMedium, color = Color(0xFF8B1E24))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FilterChip(
                selected = metodoPago == "Yape",
                onClick = { onSeleccionarMetodo("Yape") },
                label = { Text("Yape") }
            )
            FilterChip(
                selected = metodoPago == "Plin",
                onClick = { onSeleccionarMetodo("Plin") },
                label = { Text("Plin") }
            )
        }

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Pago por $metodoPago", fontWeight = FontWeight.Bold)
                Text("Confirma para emitir tu boleta simple y enviar la orden.", style = MaterialTheme.typography.bodySmall)
            }
        }

        Button(
            onClick = onConfirmarPago,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))
        ) {
            Text("Confirmar Pago y Generar Boleta")
        }
    }
}

@Composable
fun PantallaSeguimientoCliente(
    boleta: String,
    direccion: String,
    lat: Double,
    lng: Double,
    etapas: List<String>,
    indiceEtapa: Int,
    conformidad: Boolean,
    onConfirmarRecepcion: () -> Unit,
    onIrVistaRepartidor: () -> Unit,
    onNuevoPedido: () -> Unit
) {
    val progreso = (indiceEtapa + 1).toFloat() / etapas.size.toFloat()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Seguimiento del Pedido", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Boleta Simple", fontWeight = FontWeight.Bold)
                Text(boleta, style = MaterialTheme.typography.bodySmall)
                Text("Entrega: $direccion (${"%.4f".format(lat)}, ${"%.4f".format(lng)})", style = MaterialTheme.typography.bodySmall)
            }
        }

        LinearProgressIndicator(progress = { progreso }, modifier = Modifier.fillMaxWidth())

        etapas.forEachIndexed { idx, etapa ->
            val activa = idx <= indiceEtapa
            Text(
                text = etapa,
                fontWeight = if (idx == indiceEtapa) FontWeight.Bold else FontWeight.Normal,
                color = if (activa) Color(0xFF198754) else Color.Gray
            )
        }

        if (indiceEtapa == etapas.lastIndex && !conformidad) {
            Button(onClick = onConfirmarRecepcion, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Text("Confirmar Recepción Conforme")
            }
        } else if (conformidad) {
            Text("Recepción confirmada por el cliente.", fontWeight = FontWeight.Bold, color = Color(0xFF198754))
        }

        OutlinedButton(onClick = onIrVistaRepartidor, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Ir a Login Repartidor")
        }

        OutlinedButton(onClick = onNuevoPedido, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
            Text("Hacer Nuevo Pedido")
        }
    }
}

@Composable
fun PantallaPanelRepartidor(
    nombreRepartidor: String,
    pedidoActivo: Boolean,
    cliente: String,
    direccion: String,
    lat: Double,
    lng: Double,
    total: Double,
    pedidoTomado: Boolean,
    indiceEtapa: Int,
    etapas: List<String>,
    onCerrarSesion: () -> Unit,
    onTomarPedido: () -> Unit,
    onAvanzarEtapa: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Panel Repartidor", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF155E75))
            TextButton(onClick = onCerrarSesion) { Text("Salir") }
        }

        Text("Motorizado: $nombreRepartidor", style = MaterialTheme.typography.bodyMedium)

        if (!pedidoActivo) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Text(
                    text = "No hay pedidos pagados por atender en este momento.",
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Cliente: $cliente", fontWeight = FontWeight.Bold)
                    Text("Dirección: $direccion")
                    Text("Coordenadas: ${"%.5f".format(lat)}, ${"%.5f".format(lng)}", style = MaterialTheme.typography.bodySmall)
                    Text("Total pagado: S/ ${"%.2f".format(total)}", fontWeight = FontWeight.SemiBold)
                    Text("Estado actual: ${etapas[indiceEtapa]}", color = Color(0xFF198754), fontWeight = FontWeight.Bold)
                }
            }

            if (!pedidoTomado) {
                Button(
                    onClick = onTomarPedido,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF198754))
                ) {
                    Text("Recepcionar y Tomar Pedido")
                }
            } else if (indiceEtapa < etapas.lastIndex) {
                Button(
                    onClick = onAvanzarEtapa,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF155E75))
                ) {
                    Text("Avanzar a: ${etapas[indiceEtapa + 1]}")
                }
            } else {
                Text("Pedido marcado como entregado.", fontWeight = FontWeight.Bold, color = Color(0xFF198754))
            }
        }
    }
}