
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
 *  3) Exibir menu Perfil (cadastrar ponto turístico / sair);
 *  4) Realizar busca DEMONSTRATIVA por três pontos turísticos;
 *  5) Solicitar permissão de localização somente após toque;
 *  6) Exibir marcadores DEMONSTRATIVOS até integração ao back-end.
 *
 * IMPORTANTE: não altera autenticação nem bibliotecas do colega.
 * Requer: ConfirmacaoSaida.java, Activities já existentes,
 * logo_salcity.png, layouts e drawables fornecidos neste ZIP.
 * O mapa requer MAPS_API_KEY válida e Maps SDK for Android ativa.
 * =============================================================
 */

import android.Manifest;
import android.content.Intent;
import android.app.Activity;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Marker;

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

    // Catálogo demonstrativo. Temas e regiões são etiquetas de exemplo,
    // não dados oficiais dos estabelecimentos.
    private static class Ponto {
        final String nome, regiao;
        final LatLng posicao;
        final List<String> temas;
        Ponto(String nome, String regiao, LatLng posicao, String... temas) {
            this.nome = nome;
            this.regiao = regiao;
            this.posicao = posicao;
            this.temas = Arrays.asList(temas);
        }
    }
    private final List<Ponto> pontos = Arrays.asList(
            new Ponto("Pelourinho", "Pelourinho", new LatLng(-12.9718, -38.5108), "Cultura", "Histórico", "Religioso"),
            new Ponto("Farol da Barra", "Barra", new LatLng(-13.0107, -38.5328), "Praia", "Cultura", "Histórico", "Natureza"),
            new Ponto("Mercado Modelo", "Centro", new LatLng(-12.9737, -38.5133), "Cultura", "Histórico", "Gastronomia")
    );
    private final ArrayList<Marker> marcadores = new ArrayList<>();
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
                atualizarMarcadores();
            });

    // Solicita a permissão somente quando o usuário toca em "Minha localização".
    private final ActivityResultLauncher<String> permissaoLocalizacao =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedida -> {
                if (concedida) ativarMinhaLocalizacao();
                else Toast.makeText(this, "Permissão de localização não concedida", Toast.LENGTH_SHORT).show();
            });

    /**
     * Ciclo de vida: carrega a interface, liga os cliques e inicia o mapa.
     * @param savedInstanceState estado salvo pelo Android, quando disponível.
     */
    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        configurarEventos();
        inicializarMapa();
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
        /**
         * Abre a tela de perfil do usuário.
         */
        findViewById(R.id.btnPerfilHome).setOnClickListener(v -> {

            Intent intent = new Intent(
                    HomeActivity.this,
                    PerfilActivity.class
            );

            startActivity(intent);
        });
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

    /** Pesquisa local por título entre os marcadores demonstrativos, sem API externa. */
    private void pesquisarLocal() {
        String busca = campoPesquisaHome.getText().toString().trim();
        if (busca.isEmpty()) { campoPesquisaHome.setError("Digite um local"); return; }
        if (googleMap == null) { Toast.makeText(this, "Mapa indisponível", Toast.LENGTH_SHORT).show(); return; }
        String termo = busca.toLowerCase(java.util.Locale.ROOT);
        if (termo.contains("pelourinho")) focarLocal(new LatLng(-12.9718, -38.5108), "Pelourinho");
        else if (termo.contains("farol") || termo.contains("barra")) focarLocal(new LatLng(-13.0107, -38.5328), "Farol da Barra");
        else if (termo.contains("mercado") || termo.contains("modelo")) focarLocal(new LatLng(-12.9737, -38.5133), "Mercado Modelo");
        else Toast.makeText(this, "Busca demonstrativa: tente Pelourinho, Farol ou Mercado Modelo", Toast.LENGTH_LONG).show();
    }

    /**
     * Move a câmera até uma coordenada e mostra um marcador.
     * @param local coordenada geográfica do destino.
     * @param nome título do marcador.
     */
    private void focarLocal(LatLng local, String nome) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(local, 15f));
        // Apenas move a câmera: não adiciona marcadores fora dos filtros.
        for (Marker marcador : marcadores) {
            if (marcador.getTitle() != null && marcador.getTitle().equals(nome)) {
                marcador.showInfoWindow();
                break;
            }
        }
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
        atualizarMarcadores();
    }

    /** Atualiza apenas os marcadores de demonstração com tema e região. */
    private void atualizarMarcadores() {
        if (googleMap == null) return;
        for (Marker marcador : marcadores) marcador.remove();
        marcadores.clear();
        int quantidade = 0;
        for (Ponto ponto : pontos) {
            boolean temaCorresponde = temasAtivos.isEmpty();
            for (String tema : temasAtivos) {
                if (ponto.temas.contains(tema)) {
                    temaCorresponde = true;
                    break;
                }
            }
            boolean regiaoCorresponde = "Toda Salvador".equals(regiaoAtiva)
                    || ponto.regiao.equalsIgnoreCase(regiaoAtiva);
            // Centro abrange aqui o Mercado Modelo; Pelourinho tem seleção própria.
            if (temaCorresponde && regiaoCorresponde) {
                Marker marcador = googleMap.addMarker(new MarkerOptions()
                        .position(ponto.posicao).title(ponto.nome));
                if (marcador != null) marcadores.add(marcador);
                quantidade++;
            }
        }
        centralizarMapaSalvador();
        Toast.makeText(this,
                quantidade + " local(is) demonstrativo(s) no mapa. "
                        + "Orçamento e transporte serão aplicados com os dados do back-end.",
                Toast.LENGTH_LONG).show();
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

