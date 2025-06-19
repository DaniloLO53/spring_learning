<h1>Entendendo Security Filter Chain</h1>

<p>
    Primeiro, precisamos entender a diferença e as associações entre o mundo do Spring  (a classe <code>ApplicationContext</code>) que gerencia o ciclo de vida dos Beans e o mundo do <strong>Tomcat</strong>. O Tomcat é o container de servlets mais utilizado, sendo inclusive utilizado pelo Spring Boot. É ele quem gerencia o ciclo de vida e os filtros de cada servlet. Servlet, por sua vez, é uma classe que estende a classe <code>HttpServlet</code> e recebe as requisições administradas pelo Tomcat vindas do cliente. O Tomcat distribui a requisição para seu servlet correspondente a partir da URL configurada. Os dois mundos não possuem nada em comum: um não sabe da existência do outro e não compartilham nenhuma classe ou configuração.
</p>
<p>
    <strong>O problema:</strong> os filtros de segurança, que são beans implementados pelo Spring (desenvolvidos pelo desenvolvedor) precisam ser adicionados à cadeia de filtros do Tomcat. Porém, como o Tomcat irá chamar um filtro que não conhece? É aí que entra o <code>DelegatingFilterProxy</code>.
</p>

<h2>Camada 1: <code>DelegatingFilterProxy</code></h2>
<p>
    O módulo spring-web contém um inicializador que implementa <code>ServletContainerInitializer</code> que registra uma instância de <code>DelegatingFilterProxy</code> (antigamente, isso era feito via web.xml). Quando o Tomcat inicia, ele escaneia os JARs da aplicação em busca de implementações dessa interface padrão. Ele encontra o inicializador do Spring e o executa. A requisição passa pelos filtros do Tomcat até chegar no filtro <code>DelegatingFilterProxy</code>. Essa classe, apesar de fazer parte do Spring, é conhecida pelo Tomcat, pois implementa a interface <code>Filter</code> que é do Tomcat. Para o Tomcat, essa classe é apenas mais um filtro que segue as especificações de servlet.
</p>
<p>
    A classe <code>DelegatingFilterProxy</code> não possui nenhuma lógica de segurança. Sua única função é procurar no <code>ApplicationContext</code> por um bean chamado <code>springSecurityFilterChain</code> (do tipo <code>FilterChainProxy</code> e delegar a requisição para ele através do <code>doFilter()</code>.
</p>