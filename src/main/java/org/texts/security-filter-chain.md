<h1>Entendendo a arquitetura dos filtros de segurança do Spring Security</h1>

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
Cada filtro de segurança pode ser utilizado para diversas funcionalidades como cors, segurança contra exploits, autenticação, autorização, etc. Podemos, ainda, criar o nosso próprio filtro de segurança. Para isso, basta criar uma classe que estenda <code>Filter</code> ou sua versão especializadas para requisições HTTP <code>OncePerRequestFilter</code>: 
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
Nesse caso, é o filtro que irá conter a lógica responsável pela segurança. Podemos adicionar o filtro na cadeia de filtros de segurança assim:
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
            .addFilterBefore(new RobotAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
</code>
</pre>

<p>
Veja que utilizamos o método <code>addFilterBefore()</code>. Esse método adiciona um filtro antes de outro filtro que, nesse caso, é o filtro <code>UsernamePasswordAuthenticationFilter</code>, um filtro padrão para autenticação com username e password.
</p>

<h2>3. A arquitetura de autenticação no Spring Security</h2>

<h3>3.1. A classe de contexto global: <code>SecurityContextHolder</code></h3>

<p>
Essa é a classe responsável por armazenar todos os dados relevantes que caracterizam um usuário como autenticado. O Spring Security confia nessa classe para saber quem (ou o que) está autenticado ou não. Portanto, o Spring Security não se preocupa em como essa classe é populada; se a classe contém um valor, esse valor seŕa usado como um usuário autenticado.
</p>

<p>
Podemos usar essa classe diretamente para dizer se um usuário está autenticado:
</p>

<pre>
<code>
SecurityContext context = SecurityContextHolder.createEmptyContext();
Authentication authentication = new TestingAuthenticationToken("username", "password", "ROLE_USER");
context.setAuthentication(authentication);

SecurityContextHolder.setContext(context);
</code>
</pre>

<ol>
<li>
Criamos um contexto vazio a partir do método estático <code>SecurityContextHolder.createEmptyContext()</code>. É importante criar um contexto vazio, em vez de fazer <code>SecurityContextHolder.getContext().setAuthentication(authentication)</code>. Essa prática evita "race conditions" em múltiplas threads.
</li>
<br/>
<li>
Criamos uma instância de uma autenticação (<code>Authentication</code>, a qual ainda iremos discutir).
</li>
<br/>
<li>
Adicionamos o objeto de autenticação ao contexto.
</li>
<br/>
<li>
Adicionamos o contexto ao <code>SecurityContextHolder</code>, que é responsável por armazenar o contexto de autenticação.
</li>
</ol>

<p>
Para acessar o objeto autenticado, podemos fazer:
</p>

<pre>
<code>
SecurityContext context = SecurityContextHolder.getContext();
Authentication authentication = context.getAuthentication();
String username = authentication.getName();
Object principal = authentication.getPrincipal();
Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
</code>
</pre>

<h3>3.2. A classe que representa a autenticação: <code>Authentication</code></h3>

<p>
Essa classe possui dois sentidos: ela pode ser ou uma entidade já autenticada, ou uma série de dados que ainda serão validados e identificados (ou não) como uma entidade legítima que quer se autenticar.
</p>
<p>
Caso seja uma entidade já autenticada, podemos obtê-la a partir do <code>SecurityContextHolder</code>, como fizemos no exemplo acima. Caso seja uma entidade a ser autenticada, podemos passá-la para <code>AuthenticationManager</code>, que ainda falaremos. Nesse caso, <code>isAuthenticated()</code> retorna falso.
</p>
<p>
Por padrão, objetos do tipo <code>Authentication</code> possuem <strong>"Token"</strong> ao final do nome, como, por exemplo, <code>UsernamePasswordAuthenticationToken</code>.
</p>
<p>
Um Authentication possui 3 objetos relevantes:
</p>
<ol>
<li>
<code>principal:</code> é o que identifica o usuário. Quando se trata de uma autenticação com username/password, geralmente é uma instância de <code>UserDetails</code>.
</li>
<br/>
<li>
<code>credentials:</code> geralmente é o password. Em muitos casos é limpo após o usuário ser autenticado para evitar vazamentos.
</li>
<br/>
<li>
<code>authorities:</code> instâncias da classe <code>GrantedAuthority</code> que cuidam das permissões do usuário (scope e role).
</li>
</ol>

<p>
A classe <code>GrantedAuthority</code> pode ser obtida através de <code>Authorization.getAuthorities()</code> que retorna uma coleção dessa classe. Ela representa uma autoridade designada ao <code>principal</code> e, geralmente, trata-se de "roles" como <strong>"ROLE_user", "ROLE_admin"</strong>, etc.
</p>
<p>
Usualmente, as instâncias de <code>GrantedAuthority</code> são carregadas por meio de implementações da interface <code>UserDetailsService</code> 
</p>

<h3>3.3. A API geral de autenticação: <code>AuthenticationManager</code></h3>

<p>
Essa é uma classe genérica responsável por definir como o Spring Security lida com autenticação. A autenticação (instância <code>Authentication</code>) retornada é passada para o <code>SecurityContextHolder</code>. Entretanto, geralmente essa classe não é implementada diretamente pelo desenvolvedor. A implementação mais comum para essa classe é a <code>ProviderManager</code>.
</p>

<h3>3.4. O gerente dos processos de autenticação: <code>ProviderManager</code></h3>

<p>
É a implementação mais comum de <code>AuthenticationManager</code>. A sua função é orquestrar os processos de autenticação realizados pelo <code>AuthenticationProvider</code>. Cada <code>AuthenticationProvider</code> é responsável por um tipo de autenticação: podemos ter um <code>AuthenticationProvider</code> que cuida da autenticação por formulário (com DaoAuthenticationProvider) e um que cuida da autenticação por token de API (com seu <code>JwtAuthenticationProvider</code> customizado).
</p>

<p>
Portanto, essa classe não possui qualquer lógica de autenticação, mas mantém uma lista de <code>AuthenticationProvider</code> - classes especializadas em tipos de autenticação específicos. Para cada autenticação que chega, a classe verifica qual é a <code>AuthenticationProvider</code> responsável por lidar com aquele tipo de autenticação.
</p>

<h3>3.5. O autenticador especialista: <code>AuthenticationProvider</code></h3>

<p>
Trata-se de uma interface que define um contrato para processar um tipo específico de autenticação. Sua responsabilidade é receber um objeto <code>Authentication</code> (com as credenciais do usuário), validá-lo e retornar um novo objeto <code>Authentication</code> totalmente preenchido e autenticado, ou lançar uma exceção se a autenticação falhar.
</p>

<p>
Dessa forma, cada classe encapsula uma lógica de autenticação, tornando o sistema modular. O <code>DaoAuthenticationProvider</code>, por exemplo, é a implementação padrão do Spring Security que sabe como autenticar usando um <code>UserDetailsService</code> (para buscar no banco) e um <code>PasswordEncoder</code> (para comparar senhas). Para autenticar com JWT, criamos o nosso próprio <code>AuthenticationProvider</code>.
</p>

<p>
Esse contrato possui duas implementações principais e obrigatórias:
</p>

<ol>
<li>
<code>Authentication authenticate(Authentication authentication):</code> O método principal onde a lógica de validação acontece.
</li>
<br/>
<li>
<code>boolean supports(Class<?> authentication):</code> Um método crucial onde o Provider diz se ele sabe ou não lidar com um determinado tipo de objeto <code>Authentication</code>. O <code>DaoAuthenticationProvider</code>, por exemplo, só suporta <code>UsernamePasswordAuthenticationToken</code>. É aqui onde reside a especialidade de cada Provider; é o método utilizado pelo <code>ProviderManager</code> para saber se o Provider é o especialista responsável por determinada autenticação.
</li>
</ol>

<h4>3.5.1. Olhando um pouco mais de perto o <code>boolean supports(Class<?> authentication):</code></h4>

<ol>
<li>
Um usuário envia um login e senha através de um formulário. Um filtro do Spring Security (como o <code>UsernamePasswordAuthenticationFilter</code>) intercepta isso e cria um objeto do tipo <code>UsernamePasswordAuthenticationToken</code>.
</li>
<br/>
<li>
Esse token é passado para o <code>ProviderManager</code>.
</li>
<br/>
<li>
O <code>ProviderManager</code> então inicia a sua "entrevista" com os especialistas:
</li>
<ul>
<li>
Ele chega no seu <code>JwtAuthenticationProvider</code> (caso exista um) e pergunta: <code>jwtProvider.supports(UsernamePasswordAuthenticationToken.class)</code>?
</li>
<li>
O <code>JwtAuthenticationProvider</code> provavelmente responderá false, porque ele só foi programado para lidar com um <code>JwtAuthenticationToken</code>.
</li>
<li>
Ele continua e chega no <code>DaoAuthenticationProvider</code> (o especialista padrão para login/senha) e pergunta: <code>daoProvider.supports(UsernamePasswordAuthenticationToken.class)</code>?
</li>
<li>
O <code>DaoAuthenticationProvider</code> responderá <code>true</code>, pois ele foi projetado exatamente para isso.
</li>
</ul>
<br/>
<li>
Como a resposta foi true, o <code>ProviderManager</code> para de procurar e delega o trabalho, chamando <code>daoProvider.authenticate(token)</code>.
</li>
</ol>

<h3>3.6. Exemplo de fluxo da requisição</h3>

<p>
Para internalizar o que aprendemos até aqui, vamos dar uma olhada no fluxo de uma requisição:
</p>

<ol>
<li>
<strong>A Requisição Chega:</strong> O <code>AuthenticationManager</code> (que geralmente é uma instância de <code>ProviderManager</code>) recebe um objeto <code>Authentication</code> não autenticado (ex: um <code>UsernamePasswordAuthenticationToken</code> vindo de um formulário de login).
</li>
<br/>
<li>
<strong>O Gerente Pergunta:</strong> O <code>ProviderManager</code> começa a iterar sobre a sua lista de <code>AuthenticationProviders</code>.
</li>
<br/>
<li>
<strong>Busca pelo Especialista:</strong> Para cada provider na lista, o <code>ProviderManager</code> pergunta: "provider, você sabe lidar com esse tipo de token?" (chamando o método <code>provider.supports(token.getClass())).</code>
</li>
<br/>
<li>
<strong>Especialista Encontrado</strong>: Assim que um provider responde true, o <code>ProviderManager</code> para de procurar e entrega o token para esse especialista, dizendo: "Valide isso para mim!" (chamando o método <code>provider.authenticate(token))</code>.
</li>
<br/>
<li>
<strong>Validação</strong>:
<ul>
<li>
Se o provider conseguir autenticar com sucesso, ele retorna um objeto <code>Authentication</code> totalmente preenchido <code>(authenticated = true).</code> O <code>ProviderManager</code> recebe esse objeto e o retorna como resultado final do processo. Fim da história.
</li>
<br/>
<li>
Se o provider falhar (ex: senha incorreta) e lançar uma <code>AuthenticationException</code>, o <code>ProviderManager</code> pode, dependendo da configuração, continuar e tentar o próximo provider na lista que também suporte o mesmo tipo de token.
</li>
</ul>
</li>
<br/>
<li>
<strong>Nenhum Especialista Encontrado:</strong> Se o <code>ProviderManager</code> percorrer toda a lista e nenhum provider suportar o tipo de <code>Authentication</code> token fornecido, ele lançará uma <code>ProviderNotFoundException</code>.
</li>
</ol>