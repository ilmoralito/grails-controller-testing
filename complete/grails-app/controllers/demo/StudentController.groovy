// tag::controllerPackageImport[]
package demo
// end::controllerPackageImport[]

// tag::controllerImports[]
import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
// end::controllerImports[]

// tag::classDeclaration[]
@CompileStatic
class StudentController {
// end::classDeclaration[]

// tag::allowedMethods[]
    static allowedMethods = [save: 'POST',
                             update: 'PUT',
                             delete: 'DELETE',]
// end::allowedMethods[]

    // tag::injectedStudentService[]
    StudentService studentService
    // end::injectedStudentService[]

    // tag::indexAction[]
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        List<Student> studentList = studentService.list(params)
        respond studentList, model: [studentCount: studentService.count()]
    }
    // end::indexAction[]

    // tag::showAction[]
    def show(Long id) {
        if (!id) {
            notFound()
            return
        }
        respond studentService.read(id)
    }
    // end::showAction[]

    // tag::createAction[]
    def create() {
        respond studentService.create(params)
    }
    // end::createAction[]

    // tag::editAction[]
    def edit(Long id) {
        if (!id) {
            notFound()
            return
        }
        respond studentService.read(id)
    }
    // end::editAction[]

    // tag::saveAction[]
    @CompileDynamic
    def save(StudentSaveCommand cmd) {
        if (cmd.hasErrors()) { // <1>
            respond cmd.errors, [model: [student: cmd], view: 'create']
            return
        }

        Student student = studentService.save(cmd, true) // <2>
        if (student.hasErrors()) { // <3>
            respond student.errors, view:'create'
            return
        }

        request.withFormat {  // <4>
            form multipartForm {  // <5>
                flash.message = message(code: 'default.created.message', args: [message(code: 'student.label', default: 'Student'), student.id])
                redirect(action: 'show', id: student.id)
            }
            '*' { respond student, [status: CREATED] }
        }
    }
    // end::saveAction[]

    // tag::updateAction[]
    @CompileDynamic
    def update(StudentUpdateCommand cmd) {
        if ( !cmd.id ) {
            notFound()
            return
        }

        if (cmd.hasErrors()) {
            respond cmd.errors, [model: [student: cmd], view: 'edit']
            return
        }

        Student student = studentService.update(cmd, true)
        if ( student == null ) {
            notFound()
            return
        }
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'student.label', default: 'Student'), student.id])
                redirect(action: 'show', id: student.id)
            }
            '*'{ respond student, [status: OK] }
        }
    }
    // end::updateAction[]

    // tag::deleteAction[]
    @CompileDynamic
    def delete(Long id) {
        if (!id) {
            notFound()
            return
        }

        boolean found = studentService.delete(id, true)
        if (!found) {
            notFound()
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'student.label', default: 'Student'), student.id])
                redirect(action: 'index', method: 'GET')
            }
            '*'{ render status: NO_CONTENT }
        }
    }
    // end::deleteAction[]

    // tag::calculateAvgGradeAction[]
    def calculateAvgGrade() {
        BigDecimal avgGrade = studentService.calculateAvgGrade()
        render"Avg Grade is ${avgGrade}"
    }
    // end::calculateAvgGradeAction[]

    // tag::notFoundMethod[]
    @CompileDynamic
    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'student.label', default: 'Student'), params.id])
                redirect(action: 'index', method: 'GET')
            }
            '*'{ render status: NOT_FOUND }
        }
    }
    // tag::notFoundMethod[]
//tag::close[]
}
//end::close[]
