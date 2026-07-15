import os
import xml.etree.ElementTree as ET
import glob

def main():
    summary_file = os.environ.get('GITHUB_STEP_SUMMARY')
    if not summary_file:
        print("GITHUB_STEP_SUMMARY environment variable not set.")
        return

    # Path to search for reports
    reports_dir = 'backend-spring-boot/playpal/target/surefire-reports'
    xml_files = glob.glob(os.path.join(reports_dir, 'TEST-*.xml'))

    if not xml_files:
        with open(summary_file, 'a', encoding='utf-8') as f:
            f.write("### ⚠️ No se encontraron reportes de pruebas JUnit.\n")
        return

    total_tests = 0
    total_failures = 0
    total_errors = 0
    total_skipped = 0
    total_time = 0.0

    test_cases = []

    for xml_file in xml_files:
        try:
            tree = ET.parse(xml_file)
            root = tree.getroot()

            # Extract testsuites
            testsuites = [root] if root.tag == 'testsuite' else root.findall('.//testsuite')

            for suite in testsuites:
                tests = int(suite.get('tests', 0))
                failures = int(suite.get('failures', 0))
                errors = int(suite.get('errors', 0))
                skipped = int(suite.get('skipped', 0))
                time_val = float(suite.get('time', 0.0))

                total_tests += tests
                total_failures += failures
                total_errors += errors
                total_skipped += skipped
                total_time += time_val

                for tc in suite.findall('.//testcase'):
                    tc_name = tc.get('name')
                    tc_class = tc.get('classname')
                    tc_time = float(tc.get('time', 0.0))

                    status = '✅ Pasó'
                    failure_msg = ''

                    failure = tc.find('failure')
                    error = tc.find('error')
                    skipped_node = tc.find('skipped')

                    if failure is not None:
                        status = '❌ Falló'
                        failure_msg = failure.get('message', 'Fallo')
                    elif error is not None:
                        status = '💥 Error'
                        failure_msg = error.get('message', 'Error')
                    elif skipped_node is not None:
                        status = '⚠️ Omitido'
                        failure_msg = skipped_node.get('message', 'Omitido')

                    test_cases.append({
                        'class': tc_class,
                        'name': tc_name,
                        'time': tc_time,
                        'status': status,
                        'message': failure_msg
                    })
        except Exception as e:
            print(f"Error parseando {xml_file}: {e}")

    # Build markdown report
    md = []
    md.append("## 🧪 Resultados de las Pruebas (Backend)")
    md.append("")
    md.append("| Métrica | Total |")
    md.append("| :--- | :--- |")
    md.append(f"| **Pruebas Totales** | {total_tests} |")
    md.append(f"| **Éxitos ✅** | {total_tests - total_failures - total_errors - total_skipped} |")
    md.append(f"| **Fallos ❌** | {total_failures} |")
    md.append(f"| **Errores 💥** | {total_errors} |")
    md.append(f"| **Omitidas ⚠️** | {total_skipped} |")
    md.append(f"| **Tiempo de Ejecución** | {total_time:.3f} s |")
    md.append("")

    if test_cases:
        md.append("### 📝 Detalle de los Test Cases")
        md.append("")
        md.append("| Estado | Test Case / Clase | Duración | Detalles |")
        md.append("| :--- | :--- | :--- | :--- |")
        for tc in test_cases:
            class_short = tc['class'].split('.')[-1]
            name = f"**{tc['name']}** <br><sub>{tc['class']}</sub>"
            detail = tc['message'] if tc['message'] else "-"
            md.append(f"| {tc['status']} | {name} | {tc['time']:.3f} s | {detail} |")
    
    with open(summary_file, 'a', encoding='utf-8') as f:
        f.write("\n".join(md) + "\n")

if __name__ == '__main__':
    main()
