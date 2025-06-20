<h1>Entendendo a cadeia de filtros de segurança do Spring Security</h1>


<p>
    Primeiro, precisamos entender a diferença e as associações entre o mundo do Spring  (a classe <code>ApplicationContext</code>) que gerencia o ciclo de vida dos Beans e o mundo do <strong>Tomcat</strong>. O Tomcat é o container de servlets mais utilizado, sendo inclusive utilizado pelo Spring Boot. É ele quem gerencia o ciclo de vida e os filtros de cada servlet. Servlet, por sua vez, é uma classe que estende a classe <code>HttpServlet</code> e recebe as requisições administradas pelo Tomcat vindas do cliente. O Tomcat distribui a requisição para o seu servlet correspondente a partir da URL configurada. Os dois mundos não possuem nada em comum: um não sabe da existência do outro e não compartilham nenhuma classe ou configuração.
</p>
<p>
    <strong>O problema:</strong> os filtros de segurança, que são beans implementados pelo Spring (desenvolvidos pelo desenvolvedor) precisam ser adicionados à cadeia de filtros do Tomcat. Porém, como o Tomcat irá chamar um filtro que não conhece? É aí que entra o <code>DelegatingFilterProxy</code>.
</p>

<h2>1. As 3 camadas dos filtros de segurança</h2>

<ul>
<li>
<h3>Camada 1: <code>DelegatingFilterProxy</code></h3> 

<p>O módulo spring-web contém um inicializador que implementa <code>ServletContainerInitializer</code> que registra uma instância de <code>DelegatingFilterProxy</code> (antigamente, isso era feito via web.xml). Quando o Tomcat inicia, ele escaneia os JARs da aplicação em busca de implementações dessa interface padrão. Ele encontra o inicializador do Spring e o executa. A requisição passa pelos filtros do Tomcat até chegar no filtro <code>DelegatingFilterProxy</code>. Essa classe, apesar de fazer parte do Spring, é conhecida pelo Tomcat, pois implementa a interface <code>Filter</code> que é do Tomcat. Para o Tomcat, essa classe é apenas mais um filtro que segue as especificações de servlet.</p>
<p>A classe <code>DelegatingFilterProxy</code> não possui nenhuma lógica de segurança. A sua única função é procurar no <code>ApplicationContext</code> por um bean chamado <code>springSecurityFilterChain</code> (do tipo <code>FilterChainProxy</code>) e delegar a requisição para ele através do <code>doFilter()</code>.
</p>
</li>

<li>
<h3>Camada 2: <code>FilterChainProxy</code></h3>

<p>
Essa interface funciona como o cérebro que cuida das cadeias de segurança (<code>SecurityFilterChain</code>). A função desse bean não é cuidar da lógica de segurança em si, mas escolher qual <code>SecurityFilterChain</code> irá lidar com cada requisição. Dessa forma, diferentes partes da aplicação podem possuir diferentes lógicas de segurança (diferentes cadeias de segurança).
</p>

<p>
A <code>FilterChainProxy</code> mantém uma lista de <code>SecurityFilterChain</code> e cada cadeia está associada a um <code>RequestMatcher</code>, que valida um padrão de URL. Quando uma requisição chega da classe <code>DelegatingFilterProxy</code>, o  <code>FilterChainProxy</code> a inspeciona e a compara com o <code>RequestMatcher</code> de cada <code>SecurityFilterChain</code> em sua lista, na ordem (que pode ser configurada com a annotation <code>@Order</code>. Assim que ele encontra a primeira <code>SecurityFilterChain</code> cujo padrão de URL corresponde à requisição, ele para de procurar e passa a requisição para essa cadeia específica de filtros. Os filtros dessa cadeia (e somente essa) serão executados. Se nenhuma cadeia corresponder, a requisição passa sem filtros de segurança (geralmente, resultando em um erro 404 ou acesso negado, dependendo da configuração).
</p>
</li>

<li>
<h3>Camada 3: <code>SecurityFilterChain</code></h3>

<p>
O Spring Boot coleta automaticamente todos esses beans <code>SecurityFilterChain</code>, os coloca dentro de um único <code>FilterChainProxy</code> e nomeia esse bean mestre como <strong>springSecurityFilterChain</strong>. É exatamente este nome que o <code>DelegatingFilterProxy</code> procura por padrão.
</p>

<p>
Cada bean <code>SecurityFilterChain</code> é, geralmente, associado a um <strong>securityMatcher</strong>. Isso informa ao <code>FilterChainProxy</code> quando esta cadeia específica deve ser usada. Por exemplo, uma cadeia pode ser para <strong>/api/**</strong> e outra para o resto da aplicação.
</p>
<p>
Cada bean <code>SecurityFilterChain</code> possui os mecanismos de segurança a serem utilizados na requisição, a qual possui uma URL específica que dá match com a URL configurada para a cadeia de filtros. Quando uma requisição chega ao <code>FilterChainProxy</code>, ele a analisa e diz: "Ok, essa requisição é para <strong>/admin/dashboard</strong>. Conforme as minhas regras, a camada responsável por lidar com admin (<code>SecurityFilterChain</code> para <strong>/admin/**</strong>) é responsável por isso. A partir daqui, essa camada assume!". Nesse momento, a requisição é entregue ao primeiro filtro da <code>SecurityFilterChain</code> de admin, que a processa e a passa para o próximo filtro na esma cadeia, e assim por diante.
</p>
</li>
</ul>

<h2>2. O filtro de segurança</h2>
<p>
Cada filtro de segurança pode ser utilizado para diversas funcionalidades como cors, segurança contra exploits, autenticação, autorização etc. Segue um exemplo: 
</p>

<pre>
<code>
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(Customizer.withDefaults())
            .httpBasic(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
</code>
</pre>

<p>
Podemos, ainda, criar o nosso próprio filtro de segurança. Para isso, basta criar uma classe que estenda <code>Filter</code> ou sua versão especializadas para requisições HTTP <code>OncePerRequestFilter</code>: 
</p>

<pre>
<code>
public class RobotAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!Collections.list(request.getHeaderNames()).contains("x-robot-secret")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!request.getHeader("x-robot-secret").equals("beep-boop")) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.getWriter().println("Forbidden...");
            return;
        }

        RobotAuthenticationToken auth = new RobotAuthenticationToken();
        SecurityContext newContext = SecurityContextHolder.createEmptyContext();
        newContext.setAuthentication(auth);
        SecurityContextHolder.setContext(newContext);

        filterChain.doFilter(request, response);
    }
}
</code>
</pre>

<p>
Nesse caso, é o filtro que irá conter a lógica responsável pela segurança.
</p>
