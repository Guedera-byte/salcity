package com.example.salcity.activities;

/*
 * =============================================================
 * SALCITY | HOME ACTIVITY | JAVA | VERSÃO DOCUMENTADA
 * =============================================================
 * Arquivo: app/src/main/java/com/example/salcity/activities/HomeActivity.java
 * Finalidade: controlar a Home desenhada em activity_home.xml.
 *
 * RESPONSABILIDADES:
 *  1) Exibir Google Maps centralizado em Salvador;
 *  2) Abrir Filtros, Calendário, Meus Roteiros e Criar Roteiro;
 *  3) Abrir Perfil e, pelo menu Perfil, Calendário / Sair;
 *  4) Realizar busca DEMONSTRATIVA por três pontos turísticos
 *     (apenas move a câmera; não há mais marcadores/pins);
 *  5) Solicitar permissão de localização somente após toque;
 *  6) Abrir um pop-up (BottomSheet) com os detalhes de qualquer
 *     local nativo do Google Maps tocado, sem trocar de tela.
 *
 * IMPORTANTE: não altera autenticação nem bibliotecas do colega.
 * Requer: ConfirmacaoSaida.java, Activities já existentes,
 * logo_salcity.png, layouts e drawables fornecidos neste ZIP,
 * e res/layout/bottom_sheet_local.xml.
 * O mapa requer MAPS_API_KEY válida, Maps SDK for Android e
 * Places API (New) ativas para a mesma chave.
 * =============================================================
 */

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.salcity.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FetchPlaceResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.util.Log;

/** Home SalCity: interface, navegação e mapa. Sem dependência de serviços do back-end. */
public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Coordenadas aproximadas do centro de Salvador (BA).
    private static final LatLng SALVADOR = new LatLng(-12.9714, -38.5014);
    // Nível de aproximação inicial da câmera do Google Maps.
    private static final float ZOOM_INICIAL = 12.5f;
    // Referência recebida no callback onMapReady(); pode ser nula antes do carregamento.
    private GoogleMap googleMap;
    // Campo de busca visual da Home (pesquisa local de demonstração).
    private EditText campoPesquisaHome;
    // Cliente do Places SDK, usado para buscar os detalhes dos locais tocados.
    private PlacesClient placesClient;

    // Filtros recebidos da tela de filtros (serão aplicados com os dados do back-end).
    private ArrayList<String> temasAtivos = new ArrayList<>();
    private String regiaoAtiva = "Toda Salvador";
    private String transporteAtivo = null;
    private int orcamentoMin = 0, orcamentoMax = 500;

    // Recebe o resultado sem recriar a Home nem abrir a tela de roteiros.
    private final ActivityResultLauncher<Intent> resultadoFiltros =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), resultado -> {
                if (resultado.getResultCode() != Activity.RESULT_OK || resultado.getData() == null) return;
                Intent dados = resultado.getData();
                ArrayList<String> temas = dados.getStringArrayListExtra("temas");
                temasAtivos = temas == null ? new ArrayList<>() : temas;
                regiaoAtiva = dados.getStringExtra("localizacao");
                if (regiaoAtiva == null) regiaoAtiva = "Toda Salvador";
                transporteAtivo = dados.getStringExtra("transporte");
                orcamentoMin = dados.getIntExtra("orcamento_min", 0);
                orcamentoMax = dados.getIntExtra("orcamento_max", 500);
                Toast.makeText(this,
                        "Filtros salvos. Serão aplicados com os dados do back-end.",
                        Toast.LENGTH_SHORT).show();
            });

    // Solicita a permissão somente quando o usuário toca em "Minha localização".
    private final ActivityResultLauncher<String> permissaoLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
                if (concedida) ativarMinhaLocalizacao();
                else Toast.makeText(this, "Permissão de localização não concedida", Toast.LENGTH_SHORT).show();
            });

    /**
     * Ciclo de vida: carrega a interface, inicia o Places, liga os cliques e inicia o mapa.
     * @param savedInstanceState estado salvo pelo Android, quando disponível.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        inicializarPlaces();
        configurarEventos();
        inicializarMapa();
    }

    /** Inicializa o Places SDK reutilizando a chave de API declarada no AndroidManifest. */
    private void inicializarPlaces() {
        try {
            ApplicationInfo info = getPackageManager()
                    .getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
            Bundle meta = info.metaData;
            String chave = meta == null ? null : meta.getString("com.google.android.geo.API_KEY");
            if (chave == null || chave.isEmpty()) {
                throw new IllegalStateException("Chave da API não encontrada no manifest");
            }
            if (!Places.isInitialized()) Places.initializeWithNewPlacesApiEnabled(getApplicationContext(), chave);
            placesClient = Places.createClient(this);
        } catch (Exception e) {
            Toast.makeText(this, "Não foi possível iniciar o Places", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Abre um pop-up (BottomSheet) com os detalhes do local tocado, sem sair da Home.
     * @param poi local nativo do Google Maps que o usuário tocou.
     */
    private void abrirDetalhesLocal(PointOfInterest poi) {
        BottomSheetDialog dialogo = new BottomSheetDialog(this);
        View conteudo = getLayoutInflater().inflate(R.layout.local_desc, null);
        dialogo.setContentView(conteudo);

        TextView nome = conteudo.findViewById(R.id.txtNomeLocal);
        ProgressBar progresso = conteudo.findViewById(R.id.progressLocal);
        nome.setText(poi.name);
        dialogo.show();

        if (placesClient == null) { progresso.setVisibility(View.GONE); return; }

        List<Place.Field> campos = Arrays.asList(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.RATING,
                Place.Field.USER_RATINGS_TOTAL,
                Place.Field.OPENING_HOURS,
                Place.Field.PHONE_NUMBER,
                Place.Field.WEBSITE_URI,
                Place.Field.EDITORIAL_SUMMARY);

        placesClient.fetchPlace(FetchPlaceRequest.newInstance(poi.placeId, campos))
                .addOnSuccessListener(resposta -> {
                    progresso.setVisibility(View.GONE);
                    Place p = resposta.getPlace();

                    preencher(conteudo, R.id.txtDescricaoLocal, p.getEditorialSummary());
                    preencher(conteudo, R.id.txtEnderecoLocal,
                            p.getAddress() == null ? null : "📍 " + p.getAddress());
                    if (p.getRating() != null) {
                        int total = p.getUserRatingsTotal() == null ? 0 : p.getUserRatingsTotal();
                        preencher(conteudo, R.id.txtNotaLocal,
                                "⭐ " + p.getRating() + " (" + total + " avaliações)");
                    }
                    if (p.getOpeningHours() != null && !p.getOpeningHours().getWeekdayText().isEmpty()) {
                        preencher(conteudo, R.id.txtHorarioLocal,
                                "🕒 " + TextUtils.join("\n", p.getOpeningHours().getWeekdayText()));
                    }
                    preencher(conteudo, R.id.txtTelefoneLocal,
                            p.getPhoneNumber() == null ? null : "📞 " + p.getPhoneNumber());
                    preencher(conteudo, R.id.txtSiteLocal,
                            p.getWebsiteUri() == null ? null : p.getWebsiteUri().toString());
                })
                .addOnFailureListener(e -> {
                    progresso.setVisibility(View.GONE);
                    Log.e("SalCity", "Falha ao buscar local: " + e.getMessage(), e);
                    Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /** Mostra o TextView apenas quando há conteúdo para exibir. */
    private void preencher(View raiz, int idTextView, String texto) {
        if (texto == null || texto.trim().isEmpty()) return;
        TextView tv = raiz.findViewById(idTextView);
        tv.setText(texto);
        tv.setVisibility(View.VISIBLE);
    }

    /** Liga os elementos do XML aos destinos já existentes no projeto. */
    private void configurarEventos() {
        findViewById(R.id.btnFiltrosHome).setOnClickListener(v -> abrirTelaFiltros());
        findViewById(R.id.btnEventosHome).setOnClickListener(v ->
                startActivity(new Intent(this, EventosIaActivity.class)));
        findViewById(R.id.btnCalendarioHome).setOnClickListener(v -> abrirCalendario());
        findViewById(R.id.btnMeusRoteirosHome).setOnClickListener(v -> abrirMeusRoteiros());
        findViewById(R.id.btnCriarRoteiroHome).setOnClickListener(v -> abrirCriarNovoRoteiro());
        findViewById(R.id.btnCentralizarHome).setOnClickListener(v -> centralizarMapaSalvador());
        findViewById(R.id.btnMinhaLocalizacaoHome).setOnClickListener(v -> solicitarLocalizacao());

        // Abre a tela de perfil do usuário.
        findViewById(R.id.btnPerfilHome).setOnClickListener(v ->
                startActivity(new Intent(HomeActivity.this, PerfilActivity.class)));

        findViewById(R.id.btnNotificacoesHome).setOnClickListener(v ->
                Toast.makeText(this, "Notificações ainda não integradas", Toast.LENGTH_SHORT).show());

        campoPesquisaHome = findViewById(R.id.campoPesquisaHome);
        findViewById(R.id.btnBuscarHome).setOnClickListener(v -> pesquisarLocal());
        campoPesquisaHome.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) { pesquisarLocal(); return true; }
            return false;
        });
    }

    /**
     * ============================================================
     * MÉTODO: mostrarMenuPerfil()
     * ============================================================
     *
     * OBJETIVO:
     * Exibir o menu de opções do perfil do usuário.
     *
     * FUNCIONALIDADES:
     * 1. Acessar a tela de Calendário.
     * 2. Abrir a confirmação de saída do aplicativo.
     *
     * OBSERVAÇÃO:
     * A opção "Cadastrar ponto turístico" foi removida,
     * pois os pontos turísticos serão cadastrados
     * diretamente no banco de dados pela equipe.
     *
     * @param ancora Componente visual utilizado para abrir o menu.
     */
    private void mostrarMenuPerfil(View ancora) {

        // Cria o menu associado ao botão de perfil.
        PopupMenu menu = new PopupMenu(this, ancora);

        // Opção 1: abrir calendário.
        menu.getMenu().add(0, 1, 0, "Calendário");

        // Opção 2: sair do aplicativo.
        menu.getMenu().add(0, 2, 1, "Sair");

        // Configura o comportamento das opções.
        menu.setOnMenuItemClickListener(item -> {

            // Abre a tela de calendário.
            if (item.getItemId() == 1) {
                abrirCalendario();
                return true;
            }

            // Abre a confirmação de saída.
            else if (item.getItemId() == 2) {
                abrirConfirmacaoSaida();
                return true;
            }

            return false;
        });

        // Exibe o menu na tela.
        menu.show();
    }

    /** Abre os filtros de pesquisa turística. */
    private void abrirTelaFiltros() {
        resultadoFiltros.launch(new Intent(this, FiltrosActivity.class));
    }

    /** Abre a lista de roteiros do usuário. */
    private void abrirMeusRoteiros() { startActivity(new Intent(this, MeusRoteirosActivity.class)); }

    /** Abre a criação de um novo roteiro. */
    private void abrirCriarNovoRoteiro() { startActivity(new Intent(this, CriarNovoRoteiroActivity.class)); }

    /** Abre a tela do calendário. */
    private void abrirCalendario() { startActivity(new Intent(this, CalendarioActivity.class)); }

    /** Exibe o diálogo existente, com a logo original. */
    private void abrirConfirmacaoSaida() { ConfirmacaoSaida.mostrar(this, R.drawable.logo_salcity); }

    /** Busca demonstrativa por nome: move a câmera até o local, sem API externa. */
    private void pesquisarLocal() {
        String busca = campoPesquisaHome.getText().toString().trim();
        if (busca.isEmpty()) { campoPesquisaHome.setError("Digite um local"); return; }
        if (googleMap == null) { Toast.makeText(this, "Mapa indisponível", Toast.LENGTH_SHORT).show(); return; }
        String termo = busca.toLowerCase(java.util.Locale.ROOT);
        if (termo.contains("pelourinho")) focarLocal(new LatLng(-12.9718, -38.5108));
        else if (termo.contains("farol") || termo.contains("barra")) focarLocal(new LatLng(-13.0107, -38.5328));
        else if (termo.contains("mercado") || termo.contains("modelo")) focarLocal(new LatLng(-12.9737, -38.5133));
        else Toast.makeText(this, "Busca demonstrativa: tente Pelourinho, Farol ou Mercado Modelo", Toast.LENGTH_LONG).show();
    }

    /**
     * Move a câmera até uma coordenada (não adiciona marcadores).
     * @param local coordenada geográfica do destino.
     */
    private void focarLocal(LatLng local) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(local, 15f));
    }

    /** Inicializa o SupportMapFragment mantido com o ID original mapHome. */
    private void inicializarMapa() {
        SupportMapFragment fragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapHome);
        if (fragment != null) fragment.getMapAsync(this);
        else Toast.makeText(this, "Componente do mapa não encontrado", Toast.LENGTH_LONG).show();
    }

    /**
     * Callback chamado pelo SDK quando o objeto GoogleMap está disponível.
     * Não garante, sozinho, que os blocos visuais do mapa foram baixados.
     * @param map mapa retornado pelo Google Maps SDK.
     */
    @Override public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        centralizarMapaSalvador();
        googleMap.getUiSettings().setZoomGesturesEnabled(true);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        googleMap.getUiSettings().setRotateGesturesEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);

        // Toque em qualquer local nativo do Google Maps (restaurantes, pontos turísticos etc.).
        googleMap.setOnPoiClickListener(this::abrirDetalhesLocal);
    }

    /** Retorna a câmera ao centro de Salvador; ignora toque se mapa indisponível. */
    private void centralizarMapaSalvador() {
        if (googleMap != null) googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(SALVADOR, ZOOM_INICIAL));
    }

    /** Só ativa a camada de localização com permissão concedida. */
    private void solicitarLocalizacao() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            ativarMinhaLocalizacao();
        else permissaoLocalizacao.launch(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    /**
     * Habilita o ponto azul da localização somente após confirmar permissão.
     * A localização depende dos serviços de localização do dispositivo.
     */
    @SuppressWarnings("MissingPermission")
    private void ativarMinhaLocalizacao() {
        if (googleMap == null) return;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) return;
        googleMap.setMyLocationEnabled(true);
        // O botão nativo fica oculto; o ponto azul indica a localização, quando disponível.
        Toast.makeText(this, "Localização ativada no mapa", Toast.LENGTH_SHORT).show();
    }
}

