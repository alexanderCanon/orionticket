import os
import xml.etree.ElementTree as ET

services = [
    'identity-service',
    'event-management-service',
    'seating-inventory-service',
    'orders-service',
    'payments-service',
    'ticket-issuance-service',
    'access-control-service',
    'notifications-service',
    'reporting-service'
]

namespaces = {'mvn': 'http://maven.apache.org/POM/4.0.0'}
ET.register_namespace('', "http://maven.apache.org/POM/4.0.0")

deps_to_add = [
    {
        'groupId': 'org.springdoc',
        'artifactId': 'springdoc-openapi-starter-webmvc-ui',
        'version': '2.5.0'
    },
    {
        'groupId': 'io.micrometer',
        'artifactId': 'micrometer-registry-prometheus',
        'scope': 'runtime'
    },
    {
        'groupId': 'org.springframework.boot',
        'artifactId': 'spring-boot-starter-validation'
    },
    {
        'groupId': 'org.springframework.boot',
        'artifactId': 'spring-boot-starter-actuator'
    },
    {
        'groupId': 'org.flywaydb',
        'artifactId': 'flyway-core'
    },
    {
        'groupId': 'org.flywaydb',
        'artifactId': 'flyway-database-postgresql'
    },
    {
        'groupId': 'org.springframework.boot',
        'artifactId': 'spring-boot-starter-security'
    }
]

for s in services:
    pom_path = os.path.join(s, 'pom.xml')
    if not os.path.exists(pom_path):
        continue
        
    tree = ET.parse(pom_path)
    root = tree.getroot()
    
    deps_node = root.find('mvn:dependencies', namespaces)
    if deps_node is None:
        continue
        
    existing_artifacts = [node.text for node in deps_node.findall('mvn:dependency/mvn:artifactId', namespaces)]
    
    modified = False
    for dep in deps_to_add:
        if dep['artifactId'] not in existing_artifacts:
            d = ET.Element('dependency')
            g = ET.SubElement(d, 'groupId')
            g.text = dep['groupId']
            a = ET.SubElement(d, 'artifactId')
            a.text = dep['artifactId']
            if 'version' in dep:
                v = ET.SubElement(d, 'version')
                v.text = dep['version']
            if 'scope' in dep:
                s_node = ET.SubElement(d, 'scope')
                s_node.text = dep['scope']
            
            # format spacing roughly
            d.tail = '\n        '
            deps_node.append(d)
            modified = True
            print(f"Added {dep['artifactId']} to {s}")
            
    if modified:
        tree.write(pom_path, encoding='utf-8', xml_declaration=True)
